#include <jni.h>
#include <cstddef>
#include <cstdint>
#include <vector>

#include "secstr.h"
#include "signature_whitelist.h"

namespace {

struct Sha256Ctx {
    uint32_t state[8];
    uint64_t bitlen;
    uint8_t data[64];
    size_t datalen;
};

constexpr uint32_t kRoundConstants[64] = {
        0x428a2f98u, 0x71374491u, 0xb5c0fbcfu, 0xe9b5dba5u, 0x3956c25bu, 0x59f111f1u, 0x923f82a4u, 0xab1c5ed5u,
        0xd807aa98u, 0x12835b01u, 0x243185beu, 0x550c7dc3u, 0x72be5d74u, 0x80deb1feu, 0x9bdc06a7u, 0xc19bf174u,
        0xe49b69c1u, 0xefbe4786u, 0x0fc19dc6u, 0x240ca1ccu, 0x2de92c6fu, 0x4a7484aau, 0x5cb0a9dcu, 0x76f988dau,
        0x983e5152u, 0xa831c66du, 0xb00327c8u, 0xbf597fc7u, 0xc6e00bf3u, 0xd5a79147u, 0x06ca6351u, 0x14292967u,
        0x27b70a85u, 0x2e1b2138u, 0x4d2c6dfcu, 0x53380d13u, 0x650a7354u, 0x766a0abbu, 0x81c2c92eu, 0x92722c85u,
        0xa2bfe8a1u, 0xa81a664bu, 0xc24b8b70u, 0xc76c51a3u, 0xd192e819u, 0xd6990624u, 0xf40e3585u, 0x106aa070u,
        0x19a4c116u, 0x1e376c08u, 0x2748774cu, 0x34b0bcb5u, 0x391c0cb3u, 0x4ed8aa4au, 0x5b9cca4fu, 0x682e6ff3u,
        0x748f82eeu, 0x78a5636fu, 0x84c87814u, 0x8cc70208u, 0x90befffau, 0xa4506cebu, 0xbef9a3f7u, 0xc67178f2u
};

inline uint32_t rotr(uint32_t x, uint32_t n) {
    return (x >> n) | (x << (32u - n));
}

void transform(Sha256Ctx& ctx, const uint8_t block[]) {
    uint32_t m[64];
    for (uint32_t i = 0, j = 0; i < 16; ++i, j += 4) {
        m[i] = (static_cast<uint32_t>(block[j]) << 24) |
               (static_cast<uint32_t>(block[j + 1]) << 16) |
               (static_cast<uint32_t>(block[j + 2]) << 8) |
               static_cast<uint32_t>(block[j + 3]);
    }
    for (uint32_t i = 16; i < 64; ++i) {
        uint32_t s0 = rotr(m[i - 15], 7u) ^ rotr(m[i - 15], 18u) ^ (m[i - 15] >> 3u);
        uint32_t s1 = rotr(m[i - 2], 17u) ^ rotr(m[i - 2], 19u) ^ (m[i - 2] >> 10u);
        m[i] = m[i - 16] + s0 + m[i - 7] + s1;
    }
    uint32_t a = ctx.state[0], b = ctx.state[1], c = ctx.state[2], d = ctx.state[3];
    uint32_t e = ctx.state[4], f = ctx.state[5], g = ctx.state[6], h = ctx.state[7];
    for (uint32_t i = 0; i < 64; ++i) {
        uint32_t S1 = rotr(e, 6u) ^ rotr(e, 11u) ^ rotr(e, 25u);
        uint32_t ch = (e & f) ^ (~e & g);
        uint32_t t1 = h + S1 + ch + kRoundConstants[i] + m[i];
        uint32_t S0 = rotr(a, 2u) ^ rotr(a, 13u) ^ rotr(a, 22u);
        uint32_t maj = (a & b) ^ (a & c) ^ (b & c);
        uint32_t t2 = S0 + maj;
        h = g;
        g = f;
        f = e;
        e = d + t1;
        d = c;
        c = b;
        b = a;
        a = t1 + t2;
    }
    ctx.state[0] += a;
    ctx.state[1] += b;
    ctx.state[2] += c;
    ctx.state[3] += d;
    ctx.state[4] += e;
    ctx.state[5] += f;
    ctx.state[6] += g;
    ctx.state[7] += h;
}

void sha256(const uint8_t* input, size_t len, uint8_t out[32]) {
    Sha256Ctx ctx{};
    ctx.state[0] = 0x6a09e667u;
    ctx.state[1] = 0xbb67ae85u;
    ctx.state[2] = 0x3c6ef372u;
    ctx.state[3] = 0xa54ff53au;
    ctx.state[4] = 0x510e527fu;
    ctx.state[5] = 0x9b05688cu;
    ctx.state[6] = 0x1f83d9abu;
    ctx.state[7] = 0x5be0cd19u;
    ctx.bitlen = 0;
    ctx.datalen = 0;

    for (size_t i = 0; i < len; ++i) {
        ctx.data[ctx.datalen++] = input[i];
        if (ctx.datalen == 64) {
            transform(ctx, ctx.data);
            ctx.bitlen += 512;
            ctx.datalen = 0;
        }
    }

    size_t i = ctx.datalen;
    const uint64_t totalBits = ctx.bitlen + ctx.datalen * 8u;
    ctx.data[i++] = 0x80;
    if (i > 56) {
        while (i < 64) ctx.data[i++] = 0;
        transform(ctx, ctx.data);
        i = 0;
    }
    while (i < 56) ctx.data[i++] = 0;
    for (int j = 7; j >= 0; --j) {
        ctx.data[i++] = static_cast<uint8_t>((totalBits >> (j * 8)) & 0xFF);
    }
    transform(ctx, ctx.data);

    for (int j = 0; j < 4; ++j) {
        for (int s = 0; s < 8; ++s) {
            out[static_cast<size_t>(j) + static_cast<size_t>(s) * 4u] =
                    static_cast<uint8_t>((ctx.state[s] >> (24 - j * 8)) & 0xFF);
        }
    }
    secstr::wipe(&ctx, sizeof(ctx));
}

bool matchesWhitelist(const uint8_t digest[32]) {
    if (SIG_WHITELIST_COUNT == 0) {
        return true;
    }
    for (size_t entry = 0; entry < SIG_WHITELIST_COUNT; ++entry) {
        uint8_t expected[32];
        secstr::encodeDecode<32>(SIG_WHITELIST[entry], expected, 32);

        volatile uint8_t diff = 0;
        for (size_t j = 0; j < 32; ++j) {
            diff |= static_cast<uint8_t>(digest[j] ^ expected[j]);
        }
        secstr::wipe(expected, sizeof(expected));
        if (diff == 0) {
            return true;
        }
    }
    return false;
}

}  // namespace

extern "C" JNIEXPORT jboolean JNICALL
Java_com_example_appgasto_security_SecurityBridge_nativeVerifyCertificate(JNIEnv* env, jclass, jbyteArray certDer) {
    if (certDer == nullptr) {
        return JNI_FALSE;
    }
    const jsize len = env->GetArrayLength(certDer);
    if (len <= 0 || len > 1 << 20) {
        return JNI_FALSE;
    }

    std::vector<uint8_t> der(static_cast<size_t>(len));
    env->GetByteArrayRegion(certDer, 0, len, reinterpret_cast<jbyte*>(der.data()));

    uint8_t digest[32];
    sha256(der.data(), der.size(), digest);
    secstr::wipe(der.data(), der.size());

    const bool ok = matchesWhitelist(digest);
    secstr::wipe(digest, sizeof(digest));
    return ok ? JNI_TRUE : JNI_FALSE;
}
