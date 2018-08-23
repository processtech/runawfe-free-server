package ru.runa.wfe.commons.xml;

import java.io.IOException;
import java.io.Reader;

/**
 * This reader skips a possible Byte Order Marker at the start of UTF8 files which java doesn't.
 * 
 * @author michael
 */
public class BomSkippingReader extends Reader {
    private final Reader decorated;
    private final boolean rewrite;
    private final int firstchar;
    private int pos = 0;

    public BomSkippingReader(final Reader decorated) throws IOException {
        this.decorated = decorated;
        this.firstchar = decorated.read();
        this.rewrite = firstchar != 65279;
    }

    @Override
    public void close() throws IOException {
        decorated.close();
    }

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        int redone = 0;
        while (rewrite && pos < 1) {
            cbuf[off + pos++] = (char) firstchar;
            ++redone;
        }
        return redone + decorated.read(cbuf, off + redone, len - redone);
    }
}