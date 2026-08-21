#include <atomic>
#include <chrono>
#include <cstddef>
#include <cstdint>
#include <cstring>
#include <thread>
#include <time.h>

#include "secstr.h"

#if defined(__aarch64__)

namespace {

constexpr long kSysOpenat = 56;
constexpr long kSysClose = 57;
constexpr long kSysRead = 63;
constexpr long kSysPtrace = 117;
constexpr long kAtFdcwd = -100;
constexpr long kO_RDONLY = 0;

long rawSyscall(long nr, long a, long b, long c, long d) {
    register long x8 __asm__("x8") = nr;
    register long x0 __asm__("x0") = a;
    register long x1 __asm__("x1") = b;
    register long x2 __asm__("x2") = c;
    register long x3 __asm__("x3") = d;
    __asm__ volatile("svc #0"
                     : "+r"(x0)
                     : "r"(x8), "r"(x1), "r"(x2), "r"(x3)
                     : "memory", "cc");
    return x0;
}

int selfAttachTracer() {
    register long x8 __asm__("x8") = kSysPtrace;
    register long x0 __asm__("x0") = 0;
    register long x1 __asm__("x1") = 0;
    register long x2 __asm__("x2") = 0;
    register long x3 __asm__("x3") = 0;
    __asm__ volatile("svc #0"
                     : "+r"(x0)
                     : "r"(x8), "r"(x1), "r"(x2), "r"(x3)
                     : "memory", "cc");
    return static_cast<int>(x0);
}

bool tracerPidActive() {
    secstr::SecretBuf<sizeof("/proc/self/status")> statusPath("/proc/self/status");
    char path[sizeof("/proc/self/status")];
    statusPath.reveal(path);

    const long fd = rawSyscall(kSysOpenat, kAtFdcwd, reinterpret_cast<long>(path), kO_RDONLY, 0);
    secstr::wipe(path, sizeof(path));
    if (fd < 0) {
        return false;
    }

    char buf[2048];
    size_t total = 0;
    while (total + 512 < sizeof(buf)) {
        const long n = rawSyscall(kSysRead, fd, reinterpret_cast<long>(buf + total), 512, 0);
        if (n <= 0) break;
        total += static_cast<size_t>(n);
    }
    rawSyscall(kSysClose, fd, 0, 0, 0);
    buf[total] = '\0';

    bool active = false;
    const char* marker = strstr(buf, "TracerPid");
    if (marker != nullptr) {
        const char* p = marker;
        while (*p != '\0' && (*p < '0' || *p > '9')) ++p;
        long pid = 0;
        while (*p >= '0' && *p <= '9') {
            pid = pid * 10 + (*p - '0');
            ++p;
        }
        active = pid != 0;
    }
    secstr::wipe(buf, sizeof(buf));
    return active;
}

std::atomic<bool>& compromisedFlag() {
    static std::atomic<bool> flag{false};
    return flag;
}

bool sweepOnce(bool& attached) {
    if (!attached) {
        attached = selfAttachTracer() == 0;
        if (!attached) {
            return true;
        }
    }
    return tracerPidActive();
}

}  // namespace

extern "C" int gastosec_start_watchdog() {
    static std::atomic<bool> started{false};
    bool expected = false;
    if (!started.compare_exchange_strong(expected, true)) {
        return 0;
    }
    std::thread([] {
        uint32_t seed = static_cast<uint32_t>(
                std::chrono::steady_clock::now().time_since_epoch().count()) ^ 0x9E3779B9u;
        bool attached = false;
        for (;;) {
            if (sweepOnce(attached)) {
                compromisedFlag().store(true, std::memory_order_relaxed);
            }
            seed = seed * 1664525u + 1013904223u;
            const long delayMs = 20000L + static_cast<long>(seed % 70000u);
            struct timespec ts;
            ts.tv_sec = delayMs / 1000L;
            ts.tv_nsec = (delayMs % 1000L) * 1000000L;
            nanosleep(&ts, nullptr);
        }
    }).detach();
    return 1;
}

extern "C" int gastosec_is_compromised() {
    return compromisedFlag().load(std::memory_order_relaxed) ? 1 : 0;
}

#else

extern "C" int gastosec_start_watchdog() {
    return 0;
}

extern "C" int gastosec_is_compromised() {
    return 0;
}

#endif
