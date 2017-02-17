package uk.ac.st_andrews.distributo.lib;

import org.junit.*;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public abstract class TestMarshallable<T extends Marshallable> {

    public abstract List<T> makeNormalInput();

    public abstract List<T> makeExtremeInput();

    public abstract List<T> makeMarshallExceptionalInput();

    public abstract List<byte[]> makeUnmarshallExceptionalInput();

    public abstract T makeFromUnmarshall(byte[] data) throws UnmarshalException;

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

    private void checkListMarshall(List<T> items) throws MarshalException, UnmarshalException {
        if (items == null)
            return;
        for (T item : items) {
            T cpy = copy(item);
            byte[] data = item.marshal();
            item.unmarshal(data);
            assertEquals(cpy, item);
        }
    }

    @Test
    public void testMarshallNormalInput() throws MarshalException, UnmarshalException {
        checkListMarshall(normalInput);
    }

    @Test
    public void testMarshallExtremeInput() throws MarshalException, UnmarshalException {
        checkListMarshall(extremeInput);
    }

    @Test
    public void testMarshallExceptionalInput() throws MarshalException, UnmarshalException {
        List<T> items = marshallExceptionalInput;
        if (items == null)
            return;
        for (T item : items) try {
            byte[] data = item.marshal();
            fail("expected MarshalException for input " + item);
        } catch (MarshalException e) {
            // all is good
        }
    }

    @Test
    public void testUnMarshallExceptionalInput() {
        List<byte[]> data = unmarshallExceptionalInput;
        if (data == null)
            return;
        for (byte[] arr : data) try {
            T item = makeFromUnmarshall(arr);
            fail("expected UnmarshalException for data " + Arrays.toString(arr));
        } catch (UnmarshalException e) {
            // all is good
        }
    }
}
