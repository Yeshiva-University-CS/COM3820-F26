package edu.yu.parallel;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Checks your project against the handout's constraints. I run the same checks before grading;
 * a submission that breaks any of them is not graded and receives a 0.
 *
 * Generated; do not edit.
 */
class PublicConstraintsTest {

    /** Provided files: path, SHA-256 of the contents (line endings ignored), required. */
    private static final String[][] PROVIDED = {
        { "pom.xml", "cf1ead2ad722c4d9fc61755e661b35ba76b267584a27f2cbb5d24fa93da611e2", "true" },
        { "src/main/java/edu/yu/parallel/App.java", "e30a5a246c67fd8c5ca38b823dd6aeb628e8b2dd223fff104e17e628f1a5d850", "true" },
        { "src/main/java/edu/yu/parallel/BrokerService.java", "7e78698098a2541b33839d36337ac7e50c3a9ae9a47c8d578ecc6fd8e5196688", "true" },
        { "src/main/java/edu/yu/parallel/Brokerage.java", "e7c082c934f727850f03c6bef5efc966fcc1c0a4a8e71bc68edeab99fe39eacc", "true" },
        { "src/main/java/edu/yu/parallel/BrokerageSnapshot.java", "66907fe79d1cc3973a055643b63f9b59e02c65b36f9ac458dbf79b718bb5dcbf", "true" },
        { "src/main/java/edu/yu/parallel/Portfolio.java", "098ab29667565319adc2c5abd86b0dc0d2286fe9398bdfa1dcd6aad12bceddf4", "true" },
        { "src/main/java/edu/yu/parallel/PriceSource.java", "dd86728d145785c5cf4c9c96c3907ce89d0d371284b8c77bde71f63f0072b646", "true" },
        { "src/main/java/edu/yu/parallel/Stock.java", "643334761665ea6e658b94a6049822678dd02fcaacc80ac1143eff64ab0c16df", "true" },
        { "src/main/java/edu/yu/parallel/StockPrices.java", "0204dfd48892cbd2459ea755b8dcaf55e70430cb7926a842ba50673c637dcc3b", "true" },
        { "src/main/java/edu/yu/parallel/StockReader.java", "1397fce537ed2649022b450ab241f9f31f1711c64044539197eb3f23fa607152", "true" },
        { "src/main/java/edu/yu/parallel/TradeServer.java", "adb6b37aa68c6b2fcbe90692aa831ee1cbad4da52326cb62afd3580cc8cef7cf", "true" },
        { "src/test/java/edu/yu/parallel/Fixtures.java", "b9c1af9c117821015638beeca56934e9fc1cc14a86ebab7aeda84392b07915ae", "false" },
        { "src/test/java/edu/yu/parallel/PublicFunctionalTest.java", "ce340285feb109d1ab31605512a92eda2fb60c9991fdd71c523877ed032445b7", "false" },
        { "src/test/java/edu/yu/parallel/PublicHttpTest.java", "38a8a44c9c4048b2577e65daea894a138869f18fcf9db8cd39431f315079393f", "false" },
        { "src/test/java/edu/yu/parallel/TestPrices.java", "42cebc011bccc7931f18595e83880a20bac4cec099a093756739cf6e706fdd40", "false" },
        { "src/test/java/edu/yu/parallel/TestStock.java", "96a6151f6958c4a5893df68dcafde9338ccf7559c211b559f4b5d3e57ff3c507", "false" }
    };
    private static final Pattern CONCURRENT = Pattern.compile("\\b(Concurrent(HashMap|SkipListMap|SkipListSet|LinkedQueue|LinkedDeque|Map|NavigableMap)|CopyOnWrite(ArrayList|ArraySet)|(Linked|Array|Priority)Blocking(Queue|Deque)|LinkedBlockingDeque|Blocking(Queue|Deque)|LinkedTransferQueue|SynchronousQueue|DelayQueue|Collections\\s*\\.\\s*synchronized\\w+)\\b");
    private static final Pattern PRINT = Pattern.compile("\\bSystem\\s*\\.\\s*(out|err)\\b");
    private static final Pattern COMMENT_OR_STRING =
            Pattern.compile("/\\*.*?\\*/|//[^\\n]*|\"(\\\\.|[^\"\\\\\\n])*\"", Pattern.DOTALL);

    @Test
    @DisplayName("Constraints: the project follows the handout's constraints")
    void constraints() throws Exception {
        Path root = Path.of("").toAbsolutePath();
        List<String> violations = new ArrayList<>();

        String folder = folderInRepo(root);
        if (folder != null && !folder.equals("assignment-2")) {
            violations.add("pom.xml is in /" + folder + ", not /assignment-2");
        }
        List<String> provided = new ArrayList<>();
        for (String[] p : PROVIDED) {
            provided.add(p[0]);
            Path path = root.resolve(p[0]);
            if (!Files.exists(path)) {
                if (Boolean.parseBoolean(p[2])) {
                    violations.add("provided file missing: " + p[0]);
                }
            } else if (!fingerprint(path).equals(p[1])) {
                violations.add("provided file changed: " + p[0]);
            }
        }
        Path src = root.resolve("src/main/java");
        if (Files.isDirectory(src)) {
            List<Path> files;
            try (Stream<Path> walk = Files.walk(src)) {
                files = walk.filter(f -> f.toString().endsWith(".java")).sorted().toList();
            }
            for (Path f : files) {
                String rel = root.relativize(f).toString().replace('\\', '/');
                if (!rel.startsWith("src/main/java/edu/yu/parallel/")) {
                    violations.add("code outside edu.yu.parallel: " + rel);
                }
                if (provided.contains(rel)) {
                    continue;
                }
                String[] lines = stripCommentsAndStrings(Files.readString(f, StandardCharsets.UTF_8)).split("\n", -1);
                for (int n = 0; n < lines.length; n++) {
                    if (PRINT.matcher(lines[n]).find()) {
                        violations.add("System.out/err: " + rel + ":" + (n + 1));
                    }
                    Matcher m = CONCURRENT.matcher(lines[n]);
                    if (m.find()) {
                        String[] parts = m.group().split("\\.");
                        violations.add("concurrent collection " + parts[parts.length - 1].strip() + ": " + rel + ":" + (n + 1));
                    }
                }
            }
        }
        if (!violations.isEmpty()) {
            fail("Constraint violations; a submission with any violation is not graded and receives a 0:\n  - "
                    + String.join("\n  - ", violations));
        }
    }

    /** The project folder relative to the git repository root, or null if not in a git checkout. */
    private static String folderInRepo(Path root) {
        try {
            Process p = new ProcessBuilder("git", "rev-parse", "--show-toplevel")
                    .directory(root.toFile()).redirectErrorStream(true).start();
            String out = new String(p.getInputStream().readAllBytes(), StandardCharsets.UTF_8).strip();
            if (p.waitFor() != 0 || out.isEmpty()) {
                return null;
            }
            String rel = Path.of(out).toRealPath().relativize(root.toRealPath()).toString().replace('\\', '/');
            return rel.isEmpty() ? "" : rel;
        } catch (IOException | InterruptedException e) {
            return null;
        }
    }

    private static String fingerprint(Path path) throws Exception {
        byte[] bytes = new String(Files.readAllBytes(path), StandardCharsets.ISO_8859_1)
                .replace("\r\n", "\n").getBytes(StandardCharsets.ISO_8859_1);
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
    }

    /** Blanks out comments and string literals, keeping line breaks so line numbers stay right. */
    private static String stripCommentsAndStrings(String src) {
        Matcher m = COMMENT_OR_STRING.matcher(src);
        StringBuilder out = new StringBuilder();
        while (m.find()) {
            m.appendReplacement(out, Matcher.quoteReplacement(m.group().replaceAll("[^\n]", " ")));
        }
        m.appendTail(out);
        return out.toString();
    }
}
