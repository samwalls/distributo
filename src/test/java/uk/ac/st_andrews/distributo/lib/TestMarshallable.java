package uk.ac.st_andrews.distributo.lib;

import org.junit.*;
import org.junit.Test;
import uk.ac.st_andrews.distributo.lib.protocol.MarshallException;
import uk.ac.st_andrews.distributo.lib.protocol.Marshallable;
import uk.ac.st_andrews.distributo.lib.protocol.UnmarshallException;
import java.util.List;

import static org.junit.Assert.*;

public abstract class TestMarshallable<T extends Marshallable> {

    public abstract List<T> makeNormalInput();

    public abstract List<T> makeExtremeInput();

    public abstract List<T> makeMarshallExceptionalInput();

    public abstract List<byte[]> makeUnmarshallExceptionalInput();

    public abstract T makeFromUnmarshall(byte[] data) throws UnmarshallException;

    public abstract T copy(T instance);

    private List<T> normalInput, extremeInput, marshallExceptionalInput;

    private List<byte[]> unmarshallExceptionalInput;

    @Before
    public void setUp() {
        normalInput = makeNormalInput();
        extremeInput = makeExtremeInput();
        marshallExceptionalInput = makeMarshallExceptionalInput();
        unmarshallExceptionalInput = makeUnmarshallExceptionalInput();
    }

    public void checkListMarshall(List<T> items) throws MarshallException, UnmarshallException {
        if (items == null || items.size() <= 0)
            fail("no items to test");
        for (T item : items) {
            T cpy = copy(item);
            byte[] data = item.marshall();
            item.unmarshall(data);
            assertEquals(cpy, item);
        }
    }

    @Test
    public void testMarshallNormalInput() throws MarshallException, UnmarshallException {
        checkListMarshall(normalInput);
    }

    @Test
    public void testMarshallExtremeInput() throws MarshallException, UnmarshallException {
        checkListMarshall(extremeInput);
    }

    @Test
    public void testMarshallExceptionalInput() throws MarshallException, UnmarshallException {
        try {
            checkListMarshall(marshallExceptionalInput);
            fail("expected MarshallException");
        } catch (MarshallException e) {
            // all is good
        }
    }

    @Test
    public void testUnMarshallExceptionalInput() {
        List<byte[]> data = unmarshallExceptionalInput;
        if (data == null || data.size() <= 0)
            fail("no items to test");
        for (byte[] arr : data) {
            try {
                T item = makeFromUnmarshall(arr);
                fail("expected UnmarshallException");
            } catch (UnmarshallException e) {
                // all is good
            }
        }
    }
}
