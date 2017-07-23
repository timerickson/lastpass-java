package com.in2bits.shims;

/**
 * Created by Tim on 7/5/17.
 */

public class RSAParameters {
    private byte[] modulus;
    private byte[] exponent;
    private byte[] d;
    private byte[] p;
    private byte[] q;
    private byte[] dp;
    private byte[] dq;
    private byte[] inverseQ;

    public byte[] getModulus() {
        return modulus;
    }

    public byte[] getExponent() {
        return exponent;
    }

    public byte[] getD() {
        return d;
    }

    public byte[] getP() {
        return p;
    }

    public byte[] getQ() {
        return q;
    }

    public byte[] getDp() {
        return dp;
    }

    public byte[] getDq() {
        return dq;
    }

    public byte[] getInverseQ() {
        return inverseQ;
    }

    public static class Builder {
        private byte[] modulus;
        private byte[] exponent;
        private byte[] d;
        private byte[] p;
        private byte[] q;
        private byte[] dp;
        private byte[] dq;
        private byte[] inverseQ;

        public Builder setModulus(byte[] modulus) {
            this.modulus = modulus;
            return this;
        }

        public Builder setExponent(byte[] bytes) {
            this.exponent = bytes;
            return this;
        }

        public Builder setD(byte[] bytes) {
            this.d = bytes;
            return this;
        }

        public Builder setP(byte[] bytes) {
            this.p = bytes;
            return this;
        }

        public Builder setQ(byte[] bytes) {
            this.q = bytes;
            return this;
        }

        public Builder setDP(byte[] bytes) {
            this.dp = bytes;
            return this;
        }

        public Builder setDQ(byte[] bytes) {
            this.dq = bytes;
            return this;
        }

        public Builder setInverseQ(byte[] bytes) {
            this.inverseQ = bytes;
            return this;
        }

        public RSAParameters build() {
            RSAParameters result = new RSAParameters();
            result.modulus = this.modulus;
            result.exponent = this.exponent;
            result.d = this.d;
            result.p = this.p;
            result.q = this.q;
            result.dp = this.dp;
            result.dq = this.dq;
            result.inverseQ = this.inverseQ;
            return result;
        }
    }
}
