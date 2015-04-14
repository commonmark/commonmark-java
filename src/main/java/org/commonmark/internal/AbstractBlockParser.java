package org.commonmark.internal;

import org.commonmark.node.Node;

public abstract class AbstractBlockParser implements BlockParser {

	private static final BlockDidNotMatch BLOCK_DID_NOT_MATCH = new BlockDidNotMatch() {
	};

	private static final BlockMatchedAndCanBeFinalized BLOCK_MATCHED_AND_CAN_BE_FINALIZED = new BlockMatchedAndCanBeFinalized() {
	};

	@Override
	public boolean canContain(Node.Type type) {
		return false;
	}

	@Override
	public boolean shouldTryBlockStarts() {
		return false;
	}

	@Override
	public boolean acceptsLine() {
		return false;
	}

	@Override
	public void addLine(String line) {
	}

	@Override
	public void finalizeBlock(InlineParser inlineParser) {
	}

	@Override
	public void processInlines(InlineParser inlineParser) {
	}

	protected static BlockMatched blockMatched(int newOffset) {
		return new BlockMatchedImpl(newOffset);
	}

	protected static BlockDidNotMatch blockDidNotMatch() {
		return BLOCK_DID_NOT_MATCH;
	}

	protected static BlockMatchedAndCanBeFinalized blockMatchedAndCanBeFinalized() {
		return BLOCK_MATCHED_AND_CAN_BE_FINALIZED;
	}

	private static class BlockMatchedImpl implements BlockMatched {
		private final int newOffset;

		public BlockMatchedImpl(int newOffset) {
			this.newOffset = newOffset;
		}

		@Override
		public int getNewOffset() {
			return newOffset;
		}
	}
}
