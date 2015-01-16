import jdk.nashorn.internal.runtime.regexp.RegExp;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DocParser {

    private static char C_GREATERTHAN = 62;
    private static char C_NEWLINE = 10;
    private static char C_SPACE = 32;
    private static char C_OPEN_BRACKET = 91;

    private static String BLOCKTAGNAME = "(?:article|header|aside|hgroup|iframe|blockquote|hr|body|li|map|button|object|canvas|ol|caption|output|col|p|colgroup|pre|dd|progress|div|section|dl|table|td|dt|tbody|embed|textarea|fieldset|tfoot|figcaption|th|figure|thead|footer|footer|tr|form|ul|h1|h2|h3|h4|h5|h6|video|script|style)";

    private static String HTMLBLOCKOPEN = "<(?:" + BLOCKTAGNAME + "[\\s/>]" + "|" +
            "/" + BLOCKTAGNAME + "[\\s>]" + "|" + "[?!])";


    private static Pattern reHtmlBlockOpen = Pattern.compile('^' + HTMLBLOCKOPEN, 'i');

    private static Pattern reHrule = Pattern.compile("^(?:(?:\\* *){3,}|(?:_ *){3,}|(?:- *){3,}) *$");

    private static Pattern reMaybeSpecial = Pattern.compile("^[ #`~*+_=<>0-9-]");

    private static Pattern reNonSpace = Pattern.compile("[^ \t\n]");

    private static Pattern reBulletListMarker = Pattern.compile("^[*+-]( +|$)");

    private static Pattern reOrderedListMarker = Pattern.compile("^(\\d+)([.)])( +|$)");

    private static Pattern reATXHeaderMarker = Pattern.compile("^#{1,6}(?: +|$)");

    private static Pattern reCodeFence = Pattern.compile("^`{3,}(?!.*`)|^~{3,}(?!.*~)");

    private static Pattern reClosingCodeFence = Pattern.compile("^(?:`{3,}|~{3,})(?= *$)");

    private static Pattern reSetextHeaderLine = Pattern.compile("^(?:=+|-+) *$");

    private static Pattern reLineEnding = Pattern.compile("\r\n|\n|\r");


    private Node doc;
    private Node tip;
    private Node oldtip;
    private Map<Object, Object> refmap;
    private int lineNumber = 0;
    private Node lastMatchedContainer;
    private int lastLineLength = 0;

    // The main parsing function.  Returns a parsed document AST.
    public Node parse(String input) {
        this.doc = document();
        this.tip = this.doc;
        this.refmap = new HashMap<>();
//        if (this.options.time) { console.time("preparing input"); }
        String[] lines = reLineEnding.split(input);
        int len = lines.length;
        if (input.charAt(input.length() - 1) == C_NEWLINE) {
            // ignore last blank line created by final newline
            len -= 1;
        }

//        if (this.options.time) { console.timeEnd("preparing input"); }
//        if (this.options.time) { console.time("block parsing"); }
        for (int i = 0; i < len; i++) {
            this.lineNumber += 1;
            this.incorporateLine(lines[i]);
        }
        while (this.tip != null) {
            this.finalize(this.tip, len);
        }
//        if (this.options.time) { console.timeEnd("block parsing"); }
//        if (this.options.time) { console.time("inline parsing"); }
        this.processInlines(this.doc);
//        if (this.options.time) { console.timeEnd("inline parsing"); }
        return this.doc;
    }


    private Node document() {
        return new Node("Document", new int[][] {{1, 1}, {0, 0}});
    }

    // Analyze a line of text and update the document appropriately.
// We parse markdown text by calling this on each line of input,
// then finalizing the document.
    private void incorporateLine(String ln) {
        boolean all_matched = true;
        int first_nonspace;
        int offset = 0;
        int match;
        ListData data;
        boolean blank = false;
        int indent;
        int i;
        int CODE_INDENT = 4;
        boolean allClosed;

        Node container = this.doc;
        this.oldtip = this.tip;

        // replace NUL characters for security
        if (ln.indexOf("\u0000") != -1) {
            ln = ln.replace("\0", "\uFFFD");
        }

        // Convert tabs to spaces:
        ln = detabLine(ln);

        // For each containing block, try to parse the associated line start.
        // Bail out on failure: container will point to the last matching block.
        // Set all_matched to false if not all containers match.
        while (container.lastChild != null) {
            if (!container.lastChild.open) {
                break;
            }
            container = container.lastChild;

            match = matchAt(reNonSpace, ln, offset);
            if (match == -1) {
                first_nonspace = ln.length();
                blank = true;
            } else {
                first_nonspace = match;
                blank = false;
            }
            indent = first_nonspace - offset;

            switch (container.type()) {
                case "BlockQuote":
                    if (indent <= 3 && ln.charAt(first_nonspace) == C_GREATERTHAN) {
                        offset = first_nonspace + 1;
                        if (ln.charAt(offset) == C_SPACE) {
                            offset++;
                        }
                    } else {
                        all_matched = false;
                    }
                    break;

                case "Item":
                    if (indent >= container.list_data.marker_offset +
                            container.list_data.padding) {
                        offset += container.list_data.marker_offset +
                                container.list_data.padding;
                    } else if (blank) {
                        offset = first_nonspace;
                    } else {
                        all_matched = false;
                    }
                    break;

                case "Header":
                case "HorizontalRule":
                    // a header can never container > 1 line, so fail to match:
                    all_matched = false;
                    if (blank) {
                        container.last_line_blank = true;
                    }
                    break;

                case "CodeBlock":
                    if (container.fence_length > 0) { // fenced
                        // skip optional spaces of fence offset
                        i = container.fence_offset;
                        while (i > 0 && ln.charAt(offset) == C_SPACE) {
                            offset++;
                            i--;
                        }
                    } else { // indented
                        if (indent >= CODE_INDENT) {
                            offset += CODE_INDENT;
                        } else if (blank) {
                            offset = first_nonspace;
                        } else {
                            all_matched = false;
                        }
                    }
                    break;

                case "HtmlBlock":
                    if (blank) {
                        container.last_line_blank = true;
                        all_matched = false;
                    }
                    break;

                case "Paragraph":
                    if (blank) {
                        container.last_line_blank = true;
                        all_matched = false;
                    }
                    break;

                default:
            }

            if (!all_matched) {
                container = container.parent; // back up to last matching block
                break;
            }
        }

        allClosed = (container == this.oldtip);
        this.lastMatchedContainer = container;

        // Check to see if we"ve hit 2nd blank line; if so break out of list:
        if (blank && container.last_line_blank) {
            this.breakOutOfLists(container);
        }

        // Unless last matched container is a code block, try new container starts,
        // adding children to the last matched container:
        String t = container.type();
        while (t != "CodeBlock" && t != "HtmlBlock" &&
                // this is a little performance optimization:
                matchAt(reMaybeSpecial, ln, offset) != -1) {

            match = matchAt(reNonSpace, ln, offset);
            if (match == -1) {
                first_nonspace = ln.length();
                blank = true;
                break;
            } else {
                first_nonspace = match;
                blank = false;
            }
            indent = first_nonspace - offset;

            if (indent >= CODE_INDENT) {
                // indented code
                if (this.tip.type() != "Paragraph" && !blank) {
                    offset += CODE_INDENT;
                    allClosed = allClosed ||
                            this.closeUnmatchedBlocks();
                    container = this.addChild("CodeBlock", offset);
                }
                break;
            }

            offset = first_nonspace;

            char cc = ln.charAt(offset);

            Matcher matcher;
            if (cc == C_GREATERTHAN) {
                // blockquote
                offset += 1;
                // optional following space
                if (ln.charAt(offset) == C_SPACE) {
                    offset++;
                }
                allClosed = allClosed || this.closeUnmatchedBlocks();
                container = this.addChild("BlockQuote", first_nonspace);

            } else if ((matcher = reATXHeaderMarker.matcher(ln.substring(offset))).find()) {
                // ATX header
                offset += matcher.group(0).length();
                allClosed = allClosed || this.closeUnmatchedBlocks();
                container = this.addChild("Header", first_nonspace);
                container.level = matcher.group(0).trim().length(); // number of #s
                // remove trailing ###s:
                String stripped = ln.substring(offset).replaceAll("^ *#+ *$", "").replaceAll(" +#+ *$", "");
                container.strings = new ArrayList<>(Arrays.asList(stripped));
                break;

            } else if ((matcher = reCodeFence.matcher(ln.substring(offset))).find()) {
                // fenced code block
                int fence_length = matcher.group(0).length();
                allClosed = allClosed || this.closeUnmatchedBlocks();
                container = this.addChild("CodeBlock", first_nonspace);
                container.fence_length = fence_length;
                container.fence_char = matcher.group(0).charAt(0);
                container.fence_offset = indent;
                offset += fence_length;
                break;

            } else if (matchAt(reHtmlBlockOpen, ln, offset) != -1) {
                // html block
                allClosed = allClosed || this.closeUnmatchedBlocks();
                container = this.addChild("HtmlBlock", offset);
                offset -= indent; // back up so spaces are part of block
                break;

            } else if (t == "Paragraph" &&
                    container.strings.size() == 1 &&
                    ((matcher = reSetextHeaderLine.matcher(ln.substring(offset))).find())) {
                // setext header line
                allClosed = allClosed || this.closeUnmatchedBlocks();
                Node header = new Node("Header", container.sourcepos());
                header.level = matcher.group(0).charAt(0) == '=' ? 1 : 2;
                header.strings = container.strings;
                container.insertAfter(header);
                container.unlink();
                container = header;
                this.tip = header;
                offset = ln.length();
                break;

            } else if (matchAt(reHrule, ln, offset) != -1) {
                // hrule
                allClosed = allClosed || this.closeUnmatchedBlocks();
                container = this.addChild("HorizontalRule", first_nonspace);
                offset = ln.length() - 1;
                break;

            } else if ((data = parseListMarker(ln, offset, indent)) != null) {
                // list item
                allClosed = allClosed || this.closeUnmatchedBlocks();
                offset += data.padding;

                // add the list if needed
                if (t != "List" ||
                        !(listsMatch(container.list_data, data))) {
                    container = this.addChild("List", first_nonspace);
                    container.list_data = data;
                }

                // add the list item
                container = this.addChild("Item", first_nonspace);
                container.list_data = data;

            } else {
                break;

            }

        }

        // What remains at the offset is a text line.  Add the text to the
        // appropriate container.

        match = matchAt(reNonSpace, ln, offset);
        if (match == -1) {
            first_nonspace = ln.length();
            blank = true;
        } else {
            first_nonspace = match;
            blank = false;
        }
        indent = first_nonspace - offset;

        // First check for a lazy paragraph continuation:
        if (!allClosed && !blank &&
                this.tip.type() == "Paragraph" &&
                this.tip.strings.size() > 0) {
            // lazy paragraph continuation

            // foo: on DocParser? Looks like an error to me
//            this.last_line_blank = false;
            this.addLine(ln, offset);

        } else { // not a lazy continuation

            // finalize any blocks not matched
            allClosed = allClosed || this.closeUnmatchedBlocks();
            t = container.type();

            // Block quote lines are never blank as they start with >
            // and we don"t count blanks in fenced code for purposes of tight/loose
            // lists or breaking out of lists.  We also don"t set last_line_blank
            // on an empty list item.
            container.last_line_blank = blank &&
                    !(t == "BlockQuote" ||
                            t == "Header" ||
                            (t == "CodeBlock" && container.fence_length > 0) ||
                            (t == "Item" &&
                                    container.firstChild == null &&
                                    container.sourcepos()[0][0] == this.lineNumber));

            Node cont = container;
            while (cont.parent != null) {
                cont.parent.last_line_blank = false;
                cont = cont.parent;
            }

            switch (t) {
                case "HtmlBlock":
                    this.addLine(ln, offset);
                    break;

                case "CodeBlock":
                    if (container.fence_length > 0) { // fenced
                        // check for closing code fence:
                        Matcher matcher = null;
                        boolean matches = (indent <= 3 &&
                                ln.charAt(first_nonspace) == container.fence_char &&
                                (matcher = reClosingCodeFence.matcher(ln.substring(first_nonspace))).find());
                        if (matches && matcher.group(0).length() >= container.fence_length) {
                            // don"t add closing fence to container; instead, close it:
                            this.finalize(container, this.lineNumber);
                        } else {
                            this.addLine(ln, offset);
                        }
                    } else { // indented
                        this.addLine(ln, offset);
                    }
                    break;

                case "Header":
                case "HorizontalRule":
                    // nothing to do; we already added the contents.
                    break;

                default:
                    if (acceptsLines(t)) {
                        this.addLine(ln, first_nonspace);
                    } else if (blank) {
                        break;
                    } else {
                        // create paragraph container for line
                        // foo: in JS, there's a third argument, which looks like a bug
                        container = this.addChild("Paragraph", this.lineNumber);
                        this.addLine(ln, first_nonspace);
                    }
            }
        }
        this.lastLineLength = ln.length() - 1; // -1 for newline
    }

    // Finalize a block.  Close it and do any necessary postprocessing,
    // e.g. creating string_content from strings, setting the 'tight'
    // or 'loose' status of a list, and parsing the beginnings
    // of paragraphs for reference definitions.  Reset the tip to the
    // parent of the closed block.
    private int finalize(Node block, int lineNumber) {
        int pos;
        // foo: top? looks like a bug
        // var above = block.parent || this.top;

        Node above = block.parent != null ? block.parent : this.tip;
        // don't do anything if the block is already closed
        if (!block.open) {
            return 0;
        }
        block.open = false;
        block.sourcepos()[1] = new int[] {lineNumber, this.lastLineLength + 1};

        switch (block.type()) {
            case "Paragraph":
                block.string_content = join(block.strings, "\n");

                // try parsing the beginning as link reference definitions:
                while (block.string_content.charAt(0) == C_OPEN_BRACKET &&
                        (pos = this.inlineParser.parseReference(block.string_content,
                                this.refmap))) {
                    block.string_content = block.string_content.substring(pos);
                    if (isBlank(block.string_content)) {
                        block.unlink();
                        break;
                    }
                }
                break;

            case "Header":
                block.string_content = join(block.strings, "\n");
                break;

            case "HtmlBlock":
                block.literal = join(block.strings, "\n");
                break;

            case "CodeBlock":
                if (block.fence_length > 0) { // fenced
                    // first line becomes info string
                    block.info = unescapeString(block.strings[0].trim());
                    if (block.strings.length === 1) {
                        block.literal = '';
                    } else {
                        block.literal = block.strings.slice(1).join('\n') + '\n';
                    }
                } else { // indented
                    stripFinalBlankLines(block.strings);
                    block.literal = block.strings.join('\n') + '\n';
                }
                break;

            case "List":
                block.list_data.tight = true; // tight by default

                Node item = block.firstChild;
                while (item != null) {
                    // check for non-final list item ending with blank line:
                    if (endsWithBlankLine(item) && item.next) {
                        block.list_data.tight = false;
                        break;
                    }
                    // recurse into children of list item, to see if there are
                    // spaces between any of them:
                    Node subitem = item.firstChild;
                    while (subitem) {
                        if (endsWithBlankLine(subitem) && (item.next || subitem.next)) {
                            block.list_data.tight = false;
                            break;
                        }
                        subitem = subitem.next;
                    }
                    item = item.next;
                }
                break;

            default:
                break;
        }

        this.tip = above;
    };

    private String[] tabSpaces = new String[]{ "    ", "   ", "  ", " " };

    // Convert tabs to spaces on each line using a 4-space tab stop.
    private String detabLine(String text) {
        int start = 0;
        int offset;
        int lastStop = 0;

        while ((offset = text.indexOf("\t", start)) != -1) {
            int numspaces = (offset - lastStop) % 4;
            String spaces = tabSpaces[numspaces];
            text = text.substring(0, offset) + spaces + text.substring(offset + 1);
            lastStop = offset + numspaces;
            start = lastStop;
        }

        return text;
    }

    // Attempt to match a regex in string s at offset offset.
    // Return index of match or -1.
    private int matchAt(Pattern pattern, String string, int offset) {
        Matcher matcher = pattern.matcher(string);
        boolean res = matcher.find(offset);
        if (!res) {
            return -1;
        } else {
            return matcher.start();
        }
    }

    // Break out of all containing lists, resetting the tip of the
    // document to the parent of the highest list, and finalizing
    // all the lists.  (This is used to implement the "two blank lines
    // break of of all lists" feature.)
    private void breakOutOfLists(Node block) {
        Node b = block;
        Node last_list = null;
        do {
            if (b.type().equals("List")) {
                last_list = b;
            }
            b = b.parent;
        } while (b != null);

        if (last_list != null) {
            while (block != last_list) {
                this.finalize(block, this.lineNumber);
                block = block.parent;
            }
            this.finalize(last_list, this.lineNumber);
            this.tip = last_list.parent;
        }
    }

    // Add a line to the block at the tip.  We assume the tip
    // can accept lines -- that check should be done before calling this.
    private void addLine(String ln, int offset) {
        String s = ln.substring(offset);
        if (!(this.tip.open)) {
            throw new RuntimeException("Attempted to add line (" + ln + ") to closed container.");
        }
        this.tip.strings.add(s);
    }

    // Add block of type tag as a child of the tip.  If the tip can't
    // accept children, close and finalize it and try its parent,
    // and so on til we find a block that can accept children.
    private Node addChild(String tag, int offset) {
        while (!canContain(this.tip.type(), tag)) {
            this.finalize(this.tip, this.lineNumber - 1);
        }

        int column_number = offset + 1; // offset 0 = column 1
        Node newBlock = new Node(tag, new int[][] { {this.lineNumber, column_number}, {0, 0}});
        newBlock.strings = new ArrayList<>();
        newBlock.string_content = null;
        this.tip.appendChild(newBlock);
        this.tip = newBlock;
        return newBlock;
    }

    // Parse a list marker and return data on the marker (type,
    // start, delimiter, bullet character, padding) or null.
    private ListData parseListMarker(String ln, int offset, int indent) {
        String rest = ln.substring(offset);
        Matcher match;
        int spaces_after_marker;
        ListData data = new ListData(indent);
        if (reHrule.matcher(rest).find()) {
            return null;
        }
        if ((match = reBulletListMarker.matcher(rest)).find()) {
            spaces_after_marker = match.group(1).length();
            data.type = "Bullet";
            data.bullet_char = match.group(0).charAt(0);

        } else if ((match = reOrderedListMarker.matcher(rest)).find()) {
            spaces_after_marker = match.group(3).length();
            data.type = "Ordered";
            data.start = Integer.parseInt(match.group(1));
            data.delimiter = match.group(2);
        } else {
            return null;
        }
        boolean blank_item = match.group(0).length() == rest.length();
        if (spaces_after_marker >= 5 ||
                spaces_after_marker < 1 ||
                blank_item) {
            data.padding = match.group(0).length() - spaces_after_marker + 1;
        } else {
            data.padding = match.group(0).length();
        }
        return data;
    }

    // Returns true if the two list items are of the same type,
    // with the same delimiter and bullet character.  This is used
    // in agglomerating list items into lists.
    private boolean listsMatch(ListData list_data, ListData item_data) {
        return (Objects.equals(list_data.type, item_data.type) &&
                Objects.equals(list_data.delimiter, item_data.delimiter) &&
                list_data.bullet_char == item_data.bullet_char);
    }

    // Finalize and close any unmatched blocks. Returns true.
    // foo: lol?
    private boolean closeUnmatchedBlocks() {
        // finalize any blocks not matched
        while (this.oldtip != this.lastMatchedContainer) {
            Node parent = this.oldtip.parent;
            this.finalize(this.oldtip, this.lineNumber - 1);
            this.oldtip = parent;
        }
        return true;
    }

    // Returns true if parent block can contain child block.
    private static boolean canContain(String parent_type, String child_type) {
        return (parent_type.equals("Document") ||
                parent_type.equals("BlockQuote") ||
                parent_type.equals("Item") ||
                (parent_type.equals("List") && child_type.equals("Item")) );
    }

    // Returns true if block type can accept lines of text.
    private static boolean acceptsLines(String block_type) {
        return (block_type.equals("Paragraph") ||
                block_type.equals("CodeBlock"));
    }

    // Returns true if string contains only space characters.
    private boolean isBlank(String s) {
        // foo: was re.test in JS, not sure if matches
        return !(reNonSpace.matcher(s).matches());
    }

    private String join(Iterable<String> parts, String separator) {
        return String.join(separator, parts);
    }

}
