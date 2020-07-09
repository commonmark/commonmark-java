package org.commonmark.experimental;

import org.commonmark.node.Node;
import org.commonmark.node.Text;
import org.commonmark.parser.InlineParser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.commonmark.experimental.TextIdentifier.INVALID_INDEX;

public class InlineParserImpl implements InlineParser {
    private final Builder.NodeSetupSingleInstance nodeSetupSingleInstance;
    private final Map<Character, NodeGroupHandler> nodeSetupsByCharacterTrigger;
    private final Set<NodeGroupHandler> nodeSetupActives = new HashSet<>();

    private InlineParserImpl(Builder.NodeSetupSingleInstance nodeSetupSingleInstance,
                             Map<Character, NodeGroupHandler> nodeSetupsByCharacterTrigger) {
        this.nodeSetupSingleInstance = nodeSetupSingleInstance;
        this.nodeSetupsByCharacterTrigger = nodeSetupsByCharacterTrigger;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public void parse(String input, Node block) {
        nodeSetupActives.clear();

        final int length = input.length();
        final PreNode[] positionsReservedByNodePriority = new PreNode[length];
        int index = 0;

        while (index < length) {
            final char character = input.charAt(index);

            NodeGroupHandler startTrigger = nodeSetupsByCharacterTrigger.get(character);
            if (startTrigger != null) {
                nodeSetupActives.add(startTrigger);
            }

            for (NodeGroupHandler nodeSetupHandler : nodeSetupActives) {
                nodeSetupHandler.check(input, character, index, positionsReservedByNodePriority);
            }

            index++;
        }

        processNodesInLine(input, positionsReservedByNodePriority, block);
    }

    private void processNodesInLine(String text, PreNode[] positionsReservedByPriority, Node block) {
        int index = 0;
        int lastIndexEmpty = 0;

        while (index < text.length()) {
            PreNode preNode = positionsReservedByPriority[index];

            if (preNode != null) {
                if (lastIndexEmpty != index) {
                    block.appendChild(nodeSetupSingleInstance.getNodeCreator()
                            .build(text.substring(lastIndexEmpty, index), null));
                }

                block.appendChild(preNode.getNodeCreator()
                        .build(text.substring(preNode.getStartIndex(), preNode.getEndIndex()),
                                preNode.getInternalBlocks()));

                lastIndexEmpty = preNode.getEndIndex();
                index = preNode.getEndIndex();
            } else {
                index++;
            }
        }

        if (lastIndexEmpty != index) {
            block.appendChild(nodeSetupSingleInstance.getNodeCreator()
                    .build(text.substring(lastIndexEmpty, index), null));
        }
    }

    private static class DefaultLiteralNodeSetup implements NodeSetup {
        @Override
        public NodeCreator nodeCreator() {
            return new NodeCreator() {
                @Override
                public Node build(String textFound, NodePatternIdentifier.InternalBlocks[] internalBlocks) {
                    return new Text(textFound.trim());
                }
            };
        }
    }

    public static class Builder {
        private final List<TextNodeIdentifierSetup> nodeSetups = new ArrayList<>();
        private NodeSetup literalNodeSetup = new DefaultLiteralNodeSetup();

        public Builder nodeSetup(TextNodeIdentifierSetup... extensions) {
            nodeSetups.addAll(Arrays.asList(extensions));
            return this;
        }

        public Builder nodeSetup(List<TextNodeIdentifierSetup> extensions) {
            nodeSetups.addAll(extensions);
            return this;
        }

        public InlineParserImpl build() {
            return new InlineParserImpl(literalNodeSetupSingle(literalNodeSetup), bootParse(nodeSetups));
        }

        private NodeSetupSingleInstance literalNodeSetupSingle(NodeSetup literalNodeSetup) {
            return new NodeSetupSingleInstance(literalNodeSetup.nodeCreator());
        }

        private Map<Character, NodeGroupHandler> bootParse(List<TextNodeIdentifierSetup> nodeSetups) {
            orderHighestPriorityFirst(nodeSetups);

            Map<Character, NodeGroupHandler> extensionByStartBy = new HashMap<>(nodeSetups.size());

            for (TextNodeIdentifierSetup nodeSetup : nodeSetups) {
                TextIdentifier textIdentifier = nodeSetup.textIdentifier();

                NodeBreakLinePattern preProcessLine = textIdentifier.getNodeBreakLinePattern();

                NodeGroupHandler nodeGroupHandler = extensionByStartBy.get(preProcessLine.characterTrigger());
                if (nodeGroupHandler == null) {
                    nodeGroupHandler = new NodeGroupHandler();
                }

                nodeGroupHandler.add(new NodeSetupSingleInstance(textIdentifier,
                        nodeSetup.nodeCreator(),
                        nodeSetup.priority()));

                extensionByStartBy.put(preProcessLine.characterTrigger(), nodeGroupHandler);
            }
            return extensionByStartBy;
        }

        private void orderHighestPriorityFirst(List<TextNodeIdentifierSetup> nodeSetups) {
            Comparator<TextNodeIdentifierSetup> nodeSetupComparator = new Comparator<TextNodeIdentifierSetup>() {
                @Override
                public int compare(TextNodeIdentifierSetup o1, TextNodeIdentifierSetup o2) {
                    return Integer.compare(o1.priority(), o2.priority());
                }
            };
            Comparator<TextNodeIdentifierSetup> nodeSetupComparator1 = Collections.reverseOrder(nodeSetupComparator);
            Collections.sort(nodeSetups, nodeSetupComparator1);
        }

        public Builder literalNodeSetup(NodeSetup literalNodeSetup) {
            this.literalNodeSetup = literalNodeSetup;
            return this;
        }

        private static class NodeSetupSingleInstance {
            private final TextIdentifier textIdentifier;
            private final int priority;
            private final NodeCreator nodeCreator;

            public NodeSetupSingleInstance(TextIdentifier textIdentifier, NodeCreator nodeCreator, int priority) {
                this.textIdentifier = textIdentifier;
                this.nodeCreator = nodeCreator;
                this.priority = priority;
            }

            public NodeSetupSingleInstance(NodeCreator nodeCreator) {
                this(null, nodeCreator, 0);
            }

            public TextIdentifier getTextIdentifier() {
                return textIdentifier;
            }

            public int getPriority() {
                return priority;
            }

            public NodeCreator getNodeCreator() {
                return nodeCreator;
            }
        }
    }

    private static class NodeGroupHandler {
        private final List<Builder.NodeSetupSingleInstance> nodeSetups = new ArrayList<>();
        private final NodeIdentifier nodeIdentifier = new NodeIdentifier();
        private int lastIndex = INVALID_INDEX;

        public void check(final String text, final char character, final int index,
                          final PreNode[] positionsReservedByPriority) {
            nodeIdentifier.setPositions(positionsReservedByPriority);

            for (Builder.NodeSetupSingleInstance nodeSetup : nodeSetups) {
                TextIdentifier textIdentifier = nodeSetup.getTextIdentifier();
                if (lastIndex > index) {
                    textIdentifier.reset();
                }

                nodeIdentifier.setNodeSetup(nodeSetup);
                textIdentifier.checkByCharacter(text, character, index, nodeIdentifier);
            }

            lastIndex = index;
        }

        public void add(Builder.NodeSetupSingleInstance nodeSetup) {
            nodeSetups.add(nodeSetup);
        }
    }

    private static class NodeIdentifier implements NodePatternIdentifier {
        private PreNode[] positionsReservedByPriority;
        private Builder.NodeSetupSingleInstance nodeSetup;

        @Override
        public void found(int startIndex, int endIndex, InternalBlocks[] internalBlocks) {
            boolean occupied = false;
            int currentPriority = nodeSetup.getPriority();

            int j = endIndex;
            while (j > startIndex) {
                PreNode preNode = positionsReservedByPriority[--j];

                if (preNode != null) {
                    j = preNode.getStartIndex();

                    if (preNode.getPriority() >= currentPriority) {
                        occupied = true;
                        break;
                    }
                }
            }

            if (!occupied) {
                PreNode preNode = new PreNode(nodeSetup.getNodeCreator(),
                        nodeSetup.getPriority(),
                        startIndex,
                        endIndex,
                        internalBlocks
                );
                for (int i = startIndex; i < endIndex; i++) {
                    positionsReservedByPriority[i] = preNode;
                }
            }
        }

        public void setPositions(PreNode[] positionsReservedByPriority) {
            this.positionsReservedByPriority = positionsReservedByPriority;
        }

        public void setNodeSetup(Builder.NodeSetupSingleInstance nodeSetup) {
            this.nodeSetup = nodeSetup;
        }
    }
}
