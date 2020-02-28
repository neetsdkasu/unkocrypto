package neetsdkasu.util;

public final class Base64
{
    private static final int    DEFAULT_LINE_LENGTH = 76;
    private static final byte[] DEFAULT_LINE_SEPARATOR = { '\r', '\n' };
    private static final byte   PAD = '=';
    private static final byte   NO_CODE = (byte)0xFF;
    private static byte[] encodingTableRFC2045 = null;
    private static byte[] encodingTableRFC4648 = null;
    private static byte[] decodingTableRFC2045 = null;
    private static byte[] decodingTableRFC4648 = null;

    private static byte[] getEncodingTableRFC2045()
    {
        if (encodingTableRFC2045 == null)
        {
            String common = "";
            for (int c = 'A'; c <= 'Z'; c++)
            {
                common += (char)c;
            }
            for (int c = 'a'; c <= 'z'; c++)
            {
                common += (char)c;
            }
            for (int c = '0'; c <= '9'; c++)
            {
                common += (char)c;
            }
            encodingTableRFC2045 = (common + "+/").getBytes();
        }
        return encodingTableRFC2045;
    }

    private static byte[] getEncodingTableRFC4648()
    {
        if (encodingTableRFC4648 == null)
        {
            String common = "";
            for (int c = 'A'; c <= 'Z'; c++)
            {
                common += (char)c;
            }
            for (int c = 'a'; c <= 'z'; c++)
            {
                common += (char)c;
            }
            for (int c = '0'; c <= '9'; c++)
            {
                common += (char)c;
            }
            encodingTableRFC4648 = (common + "-_").getBytes();
        }
        return encodingTableRFC4648;
    }

    private static byte[] getDecodingTableRFC2045()
    {
        if (decodingTableRFC2045 == null)
        {
            decodingTableRFC2045 = new byte[256];
            for (int i = 0; i < decodingTableRFC2045.length; i++)
            {
                decodingTableRFC2045[i] = NO_CODE;
            }
            byte[] et = getEncodingTableRFC2045();
            for (int i = 0; i < et.length; i++)
            {
                decodingTableRFC2045[0xFF & (int)et[i]] = (byte)i;
            }
        }
        return decodingTableRFC2045;
    }

    private static byte[] getDecodingTableRFC4648()
    {
        if (decodingTableRFC4648 == null)
        {
            decodingTableRFC4648 = new byte[256];
            for (int i = 0; i < decodingTableRFC4648.length; i++)
            {
                decodingTableRFC4648[i] = NO_CODE;
            }
            byte[] et = getEncodingTableRFC4648();
            for (int i = 0; i < et.length; i++)
            {
                decodingTableRFC4648[0xFF & (int)et[i]] = (byte)i;
            }
        }
        return decodingTableRFC4648;
    }

    private Base64() {}

    public static Encoder getEncoder()
    {
        return new Encoder(getEncodingTableRFC2045(), true);
    }

    public static Decoder getDecoder()
    {
        return new Decoder(getDecodingTableRFC2045(), false);
    }

    public static Encoder getMimeEncoder(int lineLength, byte[] lineSeparator)
    {
        return new Encoder(getEncodingTableRFC2045(), true, lineLength, lineSeparator);
    }

    public static Encoder getMimeEncoder()
    {
        return getMimeEncoder(DEFAULT_LINE_LENGTH, DEFAULT_LINE_SEPARATOR);
    }

    public static Decoder getMimeDecoder()
    {
        return new Decoder(getDecodingTableRFC2045(), true);
    }

    public static Encoder getUrlEncoder()
    {
        return new Encoder(getEncodingTableRFC4648(), true);
    }

    public static Decoder getUrlDecoder()
    {
        return new Decoder(getDecodingTableRFC4648(), false);
    }

    public static class Encoder
    {
        private final byte[]  code;
        private final boolean padding;
        private final int     lineLength;
        private final byte[]  lineSeparator;

        private Encoder(byte[] code, boolean padding)
        {
            this.code = code;
            this.padding = padding;
            this.lineLength = 0;
            this.lineSeparator = null;
        }

        private Encoder(byte[] code, boolean padding, int lineLength, byte[] lineSeparator)
        {
            this(true, code, padding, lineLength, lineSeparator);
        }

        private Encoder(boolean check, byte[] code, boolean padding, int lineLength, byte[] lineSeparator)
        {
            this.code = code;
            this.padding = padding;
            this.lineLength = (lineLength >> 2) << 2;
            this.lineSeparator = this.lineLength > 0 ? lineSeparator : null;
            if (!check)
            {
                return;
            }
            for (int j = 0; j < lineSeparator.length; j++)
            {
                byte sep = lineSeparator[j];
                if (sep == Base64.PAD)
                {
                    throw new IllegalArgumentException("lineSeparator: " + sep);
                }
                for (int i = 0; i < code.length; i++)
                {
                    if (code[i] == sep)
                    {
                        throw new IllegalArgumentException("lineSeparator: " + sep);
                    }
                }
            }
        }

        public Encoder withoutPadding()
        {
            return new Encoder(false, code, false, lineLength, lineSeparator);
        }

        public String encodeToString(byte[] src)
        {
            return new String(encode(src));
        }

        public byte[] encode(byte[] src)
        {
            int size = (src.length * 8 + 5) / 6;
            if (padding)
            {
                size = ((size + 3) >> 2) << 2;
            }
            if (lineSeparator != null)
            {
                int d = (size + lineLength - 1) / lineLength;
                if (d > 1)
                {
                    size += (d - 1) * lineSeparator.length;
                }
            }
            byte[] ret = new byte[size];
            int rem = 0, bits = 0, p = 0, nextp = lineLength;
            for (int i = 0; i < src.length; i++)
            {
                rem = (0xFF & (int)src[i]) | (rem << 8);
                bits += 8;
                while (bits >= 6)
                {
                    bits -= 6;
                    ret[p] = code[rem >> bits];
                    rem &= (1 << bits) - 1;
                    p++;
                    if (lineSeparator != null && p == nextp && p < ret.length)
                    {
                        for (int j = 0; j < lineSeparator.length; j++)
                        {
                            ret[p] = lineSeparator[j];
                            p++;
                        }
                        nextp += lineLength + lineSeparator.length;
                    }
                }
            }
            if (bits > 0)
            {
                ret[p] = code[rem << (6 - bits)];
                p++;
            }
            while (p < ret.length)
            {
                ret[p] = Base64.PAD;
                p++;
            }
            return ret;
        }
    }

    public static class Decoder
    {
        private final byte[] table;
        private final boolean mime;
        private Decoder(byte[] table, boolean mime)
        {
            this.table = table;
            this.mime = mime;
        }

        public byte[] decode(String src)
        {
            return decode(src.getBytes());
        }

        public byte[] decode(byte[] src)
        {
            byte[] ret = new byte[(src.length * 6) / 8];
            int rem = 0, bits = 0, pos = 0;
            int size = 0;
            int srcpos = src.length;
            for (int i = 0; i < src.length; i++)
            {
                byte code = table[(int)src[i]];
                if (code != Base64.NO_CODE)
                {
                    rem = (int)code | (rem << 6);
                    bits += 6;
                    while (bits >= 8)
                    {
                        bits -= 8;
                        ret[pos] = (byte)(rem >> bits);
                        rem &= (1 << bits) - 1;
                        pos++;
                    }
                    size++;
                    continue;
                }
                if (src[i] == Base64.PAD)
                {
                    srcpos = i;
                    break;
                }
                if (!mime || (size & 3) != 0)
                {
                    throw new IllegalArgumentException("invalid char: pos: " + i + ", code: " + src[i]);
                }
            }
            if (pos != (size * 6) / 8 || bits >= 6)
            {
                throw new IllegalArgumentException("invalid src length");
            }
            int paddingSize = size;
            for (int i = srcpos; i < src.length; i++)
            {
                if (table[(int)src[i]] != Base64.NO_CODE)
                {
                    throw new IllegalArgumentException("invalid char: pos: " + i + ", code: " + src[i]);
                }
                if (src[i] == Base64.PAD)
                {
                    paddingSize++;
                    if (paddingSize <= (((size + 3) >> 2) << 2))
                    {
                        continue;
                    }
                    throw new IllegalArgumentException("invalid char: pos: " + i + ", code: " + src[i]);
                }
                if (!mime || paddingSize < (((size + 3) >> 2) << 2))
                {
                    throw new IllegalArgumentException("invalid char: pos: " + i + ", code: " + src[i]);
                }
            }
            if (size != paddingSize && paddingSize != (((size + 3) >> 2) << 2))
            {
                throw new IllegalArgumentException("invalid padding length");
            }
            if (ret.length == pos)
            {
                return ret;
            }
            byte[] tmp = new byte[pos];
            System.arraycopy(ret, 0, tmp, 0, tmp.length);
            return tmp;
        }
    }
}