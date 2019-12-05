package examples.websocket;

import io.github.zhtmf.annotations.enums.NumericEnum;

public enum OpCodes implements NumericEnum{
    CONTINUATION {
        @Override
        public long getValue() {
            return 0;
        }
    },
    TEXT {
        @Override
        public long getValue() {
            return 1;
        }
    },
    BINARY {
        @Override
        public long getValue() {
            return 2;
        }
    },
    CLOSE {
        @Override
        public long getValue() {
            return 8;
        }
    },
    PING {
        @Override
        public long getValue() {
            return 9;
        }
    },
    PONG {
        @Override
        public long getValue() {
            return 0xA;
        }
    }
    ;

    @Override
    abstract public long getValue();
}
