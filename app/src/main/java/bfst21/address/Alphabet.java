package bfst21.address;


/**
 * Alphabet class used to improve memory usage for our address TST.
 * <p>
 * This code was written by Philip Flyvholm, all credit goes to him.
 */
public enum Alphabet {

    //41 different chars excepted A-Å+0-9+" "+.+,
    A('a', (byte) 0),
    B('b', (byte) 1),
    C('c', (byte) 2),
    D('d', (byte) 3),
    E('e', (byte) 4),
    F('f', (byte) 5),
    G('g', (byte) 6),
    H('h', (byte) 7),
    I('i', (byte) 8),
    J('j', (byte) 9),
    K('k', (byte) 10),
    L('l', (byte) 11),
    M('m', (byte) 12),
    N('n', (byte) 13),
    O('o', (byte) 14),
    P('p', (byte) 15),
    Q('q', (byte) 16),
    R('r', (byte) 17),
    S('s', (byte) 18),
    T('t', (byte) 19),
    U('u', (byte) 20),
    V('v', (byte) 21),
    W('w', (byte) 22),
    X('x', (byte) 23),
    Y('y', (byte) 24),
    Z('z', (byte) 25),
    AE('æ', (byte) 26), //AE = æ
    OO('ø', (byte) 27), //OO = ø
    AA('å', (byte) 28), //AA = å
    ZERO('0', (byte) 29),
    ONE('1', (byte) 30),
    TWO('2', (byte) 31),
    THREE('3', (byte) 32),
    FOUR('4', (byte) 33),
    FIVE('5', (byte) 34),
    SIX('6', (byte) 35),
    SEVEN('7', (byte) 36),
    EIGHT('8', (byte) 37),
    NINE('9', (byte) 38),
    SPACE(' ', (byte) 39),
    DOT('.', (byte) 40),
    COMMA(',', (byte) 41),
    ACUTE('é', (byte) 42), // é
    UMLAUT('ü', (byte) 43), // ü
    HYPHEN('-', (byte) 44), // -
    APOSTROPHE('\'', (byte) 45); // '

    private final char character;
    private final byte associatedByte;

    Alphabet(char character, byte associatedByte) {
        this.character = character;
        this.associatedByte = associatedByte;
    }

    /**
     * Get the byte associated with the character.
     *
     * @param character A lowercase char.
     * @return The associated byte with the char. -1 if invalid.
     */
    public static byte getByteValue(char character) {
        for (Alphabet value : Alphabet.values()) {
            if (value.character == character) {
                return value.associatedByte;
            }
        }
        return (byte) -1;
    }

    /**
     * Get the char associated with the byte.
     *
     * @return The associated char with the byte. '0' if invalid.
     */
    public static char getCharValue(byte val) {
        for (Alphabet value : Alphabet.values()) {
            if (value.associatedByte == val) {
                return value.character;
            }
        }
        return '0';
    }
}