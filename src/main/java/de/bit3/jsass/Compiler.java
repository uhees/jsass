package de.bit3.jsass;

import com.sun.jna.Native;
import de.bit3.jsass.context.Context;
import de.bit3.jsass.context.ContextFactory;
import de.bit3.jsass.context.FileContext;
import de.bit3.jsass.context.StringContext;
import org.apache.commons.io.Charsets;
import sass.SassLibrary;

import java.io.File;
import java.nio.charset.Charset;

/**
 * Hello world!
 */
public class Compiler {
    /**
     * The default defaultCharset that is used for compiling strings.
     */
    public final Charset defaultCharset = Charsets.UTF_8;

    /**
     * SASS library adapter.
     */
    private final SassLibrary SASS = (SassLibrary) Native.loadLibrary("sass", SassLibrary.class);

    /**
     * The context factory.
     */
    private final ContextFactory contextFactory;

    /**
     * Create new compiler.
     */
    public Compiler() {
        contextFactory = new ContextFactory(SASS);
    }

    /**
     * Compile string.
     *
     * @param string  The input string.
     * @param options The compile options.
     * @return The compilation output.
     * @throws CompilationException If the compilation failed.
     */
    public Output compileString(String string, Options options) throws CompilationException {
        return compileString(string, defaultCharset, null, null, options);
    }

    /**
     * Compile string.
     *
     * @param string  The input string.
     * @param charset The defaultCharset of the input string.
     * @param options The compile options.
     * @return The compilation output.
     * @throws CompilationException If the compilation failed.
     */
    public Output compileString(String string, Charset charset, Options options) throws CompilationException {
        return compileString(string, charset, null, null, options);
    }

    /**
     * Compile string.
     *
     * @param string     The input string.
     * @param inputPath  The input path.
     * @param outputPath The output path.
     * @param options    The compile options.
     * @return The compilation output.
     * @throws CompilationException If the compilation failed.
     */
    public Output compileString(String string, File inputPath, File outputPath, Options options) throws CompilationException {
        return compileString(string, defaultCharset, inputPath, outputPath, options);
    }

    /**
     * Compile string.
     *
     * @param string     The input string.
     * @param charset    The defaultCharset of the input string.
     * @param inputPath  The input path.
     * @param outputPath The output path.
     * @param options    The compile options.
     * @return The compilation output.
     * @throws CompilationException If the compilation failed.
     */
    public Output compileString(String string, Charset charset, File inputPath, File outputPath, Options options) throws CompilationException {
        StringContext context = new StringContext(string, charset, inputPath, outputPath, options);

        return compile(context);
    }

    /**
     * Compile file.
     *
     * @param inputPath  The input path.
     * @param outputPath The output path.
     * @param options    The compile options.
     * @return The compilation output.
     * @throws CompilationException If the compilation failed.
     */
    public Output compileFile(File inputPath, File outputPath, Options options) throws CompilationException {
        FileContext context = new FileContext(inputPath, outputPath, options);
        return compile(context);
    }

    /**
     * Compile context.
     *
     * @param context The context.
     * @return The compilation output.
     * @throws CompilationException If the compilation failed.
     */
    public Output compile(Context context) throws CompilationException {
        if (context instanceof FileContext) {
            return compile((FileContext) context);
        }

        if (context instanceof StringContext) {
            return compile((StringContext) context);
        }

        throw new RuntimeException(
                String.format(
                        "Context type \"%s\" is not supported",
                        null == context ? "null" : context.getClass().getName()
                )
        );
    }

    /**
     * Compile a string context.
     *
     * @param context The string context.
     * @return The compilation output.
     * @throws CompilationException If the compilation failed.
     */
    public Output compile(StringContext context) throws CompilationException {
        // create file context
        SassLibrary.Sass_Data_Context dataContext = null;

        try {
            dataContext = contextFactory.create(context);

            // compile file
            SASS.sass_compile_data_context(dataContext);

            // check error status
            SassLibrary.Sass_Context libsassContext = SASS.sass_data_context_get_context(dataContext);
            checkErrorStatus(libsassContext);

            return createOutput(libsassContext);
        } finally {
            if (null != dataContext) {
                // free context
                SASS.sass_delete_data_context(dataContext);
            }
        }
    }

    /**
     * Compile file.
     *
     * @param context The file context.
     * @return The compilation output.
     * @throws CompilationException If the compilation failed.
     */
    public Output compile(FileContext context) throws CompilationException {
        // create file context
        SassLibrary.Sass_File_Context fileContext = null;

        try {
            // create context
            fileContext = contextFactory.create(context);

            // compile file
            SASS.sass_compile_file_context(fileContext);

            // check error status
            SassLibrary.Sass_Context libsassContext = SASS.sass_file_context_get_context(fileContext);
            checkErrorStatus(libsassContext);

            return createOutput(libsassContext);
        } finally {
            if (null != fileContext) {
                // free context
                SASS.sass_delete_file_context(fileContext);
            }
        }
    }

    /**
     * Check the error status.
     *
     * @param context The sass context.
     * @throws CompilationException If the error status is not <em>0</em>.
     */
    private void checkErrorStatus(SassLibrary.Sass_Context context) throws CompilationException {
        int status = SASS.sass_context_get_error_status(context);

        if (status != 0) {
            String file = SASS.sass_context_get_error_file(context);
            String message = SASS.sass_context_get_error_message(context);

            throw new CompilationException(status, file + ": " + message);
        }
    }

    /**
     * Create output from context.
     *
     * @param context The sass context.
     * @return The output.
     */
    private Output createOutput(SassLibrary.Sass_Context context) {
        String css       = SASS.sass_context_get_output_string(context);
        String sourceMap = SASS.sass_context_get_source_map_string(context);

        return new Output(css, sourceMap);
    }

}
