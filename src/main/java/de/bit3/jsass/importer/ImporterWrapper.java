package de.bit3.jsass.importer;

import com.ochafik.lang.jnaerator.runtime.NativeSize;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;
import de.bit3.jsass.context.Context;
import sass.SassLibrary;

import java.nio.ByteBuffer;
import java.util.Collection;

public class ImporterWrapper implements SassLibrary.Sass_C_Import_Fn {
    /**
     * SASS library adapter.
     */
    private final SassLibrary SASS;
    private final Context        originalContext;
    private final Importer       importer;

    public ImporterWrapper(SassLibrary SASS, Context originalContext, Importer importer) {
        this.SASS = SASS;
        this.originalContext = originalContext;
        this.importer = importer;
    }

    @Override
    public PointerByReference apply(Pointer url, Pointer prev, Pointer cookie) {
        Collection<Import> imports = importer.apply(url.getString(0), prev.getString(0), originalContext);

        // return 0 to let libsass handle the import itself
        if (null == imports) {
            return null;
        }

        PointerByReference list = SASS.sass_make_import_list(new NativeSize(imports.size()));

        int index = 0;
        String path;
        String base;
        byte[] bytes;
        Memory sourceMemory;
        ByteBuffer source;
        Memory sourceMapMemory;
        ByteBuffer sourceMap;

        for (Import importSource : imports) {
            path = importSource.getUri().toString();

            base = null == importSource.getBase() ? "" : importSource.getBase().toString();

            if (null == importSource.getContents()) {
                sourceMemory = new Memory(1);
                sourceMemory.setByte(0, (byte) 0);
            } else {
                bytes = importSource.getContents().getBytes(importSource.getContentsCharset());
                sourceMemory = new Memory(bytes.length + 1);
                sourceMemory.write(0, bytes, 0, bytes.length);
                sourceMemory.setByte(bytes.length, (byte) 0);
            }

            source = sourceMemory.getByteBuffer(0, sourceMemory.size());

            if (null == importSource.getSourceMap()) {
                sourceMapMemory = new Memory(1);
                sourceMapMemory.setByte(0, (byte) 0);
            } else {
                bytes = importSource.getSourceMap().getBytes(importSource.getSourceMapCharset());
                sourceMapMemory = new Memory(bytes.length + 1);
                sourceMapMemory.write(0, bytes, 0, bytes.length);
                sourceMapMemory.setByte(bytes.length, (byte) 0);
            }

            sourceMap = sourceMapMemory.getByteBuffer(0, sourceMapMemory.size());

            SassLibrary.Sass_Import entry = SASS.sass_make_import(path, base, source, sourceMap);
            SASS.sass_import_set_list_entry(list, new NativeSize(index), entry);
            index++;
        }

        return list;
    }
}
