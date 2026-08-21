#pragma once

#include <cstddef>
#include <cstdint>

namespace secstr {

constexpr uint8_t kBaseKey = 0x5A;

inline uint8_t byteKey(size_t i) {
    return static_cast<uint8_t>(kBaseKey + i * 31u);
}

inline void wipe(volatile void* p, size_t n) {
    volatile uint8_t* b = static_cast<volatile uint8_t*>(p);
    for (size_t i = 0; i < n; ++i) {
        b[i] = 0;
    }
    asm volatile("" : : "r"(b) : "memory");
}

template <size_t N>
void encodeDecode(const uint8_t* src, volatile uint8_t* dst, size_t n) {
    for (size_t i = 0; i < n && i < N; ++i) {
        dst[i] = static_cast<uint8_t>(src[i] ^ byteKey(i));
    }
}

template <size_t N>
struct SecretBuf {
    volatile uint8_t buf[N];

    explicit SecretBuf(const char (&plain)[N]) {
        for (size_t i = 0; i < N; ++i) {
            buf[i] = static_cast<uint8_t>(plain[i]) ^ byteKey(i);
        }
    }

    void reveal(char* out) const {
        for (size_t i = 0; i < N; ++i) {
            out[i] = static_cast<char>(buf[i] ^ byteKey(i));
        }
    }

    ~SecretBuf() {
        wipe(buf, N);
    }

    SecretBuf(const SecretBuf&) = delete;
    SecretBuf& operator=(const SecretBuf&) = delete;
};

}  // namespace secstr
