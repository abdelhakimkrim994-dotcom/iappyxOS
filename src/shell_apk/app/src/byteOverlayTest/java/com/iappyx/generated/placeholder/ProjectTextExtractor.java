package com.iappyx.generated.placeholder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/** Conservative local text extractor. Original project files are never modified. */
public final class ProjectTextExtractor {
    private static final int MAX_INDEX_BYTES = 2 * 1024 * 1024;
    private static final Set<String> TEXT_EXTENSIONS = new HashSet<>(Arrays.asList(
        "txt","md","markdown","csv","tsv","json","xml","html","htm","css","js","mjs",
        "java","kt","kts","py","yml","yaml","properties","gradle","sql","sh","log",
        "ts","tsx","jsx","c","h","cc","cpp","hpp","rb","go","rs","php","ini","toml",
        "conf","config","env","gitignore","gitattributes"
    ));
    public static final class Result {
        public final String status;
        public final String text;
        Result(String status, String text) { this.status = status; this.text = text; }
    }
    private ProjectTextExtractor() {}
    public static Result extract(File file, String displayName) {
        if (file == null || !file.isFile()) return new Result("error", "");
        String name = displayName == null ? "" : displayName;
        int dot = name.lastIndexOf('.');
        String ext = dot < 0 ? "" : name.substring(dot + 1).toLowerCase(Locale.ROOT);
        if (!TEXT_EXTENSIONS.contains(ext)) return new Result("unsupported", "");
        if (file.length() > MAX_INDEX_BYTES) return new Result("too_large", "");
        try (FileInputStream in = new FileInputStream(file);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[8192];
            int count;
            while ((count = in.read(buffer)) != -1) {
                out.write(buffer, 0, count);
                if (out.size() > MAX_INDEX_BYTES) return new Result("too_large", "");
            }
            byte[] bytes = out.toByteArray();
            for (int i = 0; i < Math.min(bytes.length, 8192); i++) {
                if (bytes[i] == 0) return new Result("unsupported", "");
            }
            int offset = bytes.length >= 3 && (bytes[0] & 255) == 239
                    && (bytes[1] & 255) == 187 && (bytes[2] & 255) == 191 ? 3 : 0;
            String text = new String(bytes, offset, bytes.length - offset,
                    java.nio.charset.StandardCharsets.UTF_8);
            return new Result("indexed", text);
        } catch (Exception ignored) {
            return new Result("error", "");
        }
    }
}
