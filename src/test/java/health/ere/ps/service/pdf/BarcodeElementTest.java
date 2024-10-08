package health.ere.ps.service.pdf;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.awt.Dimension;

import org.apache.fop.apps.FOPException;
import org.junit.jupiter.api.Test;
import org.krysalis.barcode4j.impl.datamatrix.DataMatrixSymbolInfo;
import org.krysalis.barcode4j.impl.datamatrix.SymbolShapeHint;

public class BarcodeElementTest {
    @Test
    public void test() throws FOPException {
        int dataCodewords=719;
        assertThrows(IllegalArgumentException.class, () ->
            lookup(dataCodewords)
        );
    }

    @Test
    public void test2() throws FOPException {
        int dataCodewords=698;
        assertThrows(IllegalArgumentException.class, () ->
            lookup(dataCodewords)
        );  
    }

    private void lookup(int dataCodewords) {
        return lookup(dataCodewords, 90, 100);
    }
    private void lookup(int dataCodewords, int minSizeInt, int maxSizeInt) {
        SymbolShapeHint shape = SymbolShapeHint.FORCE_SQUARE;
        Dimension minSize = new Dimension(minSizeInt, minSizeInt);
        Dimension maxSize = new Dimension(maxSizeInt, maxSizeInt);

        DataMatrixSymbolInfo.lookup(dataCodewords,
            shape, minSize, maxSize, true);
    }

    @Test
    public void test3() throws FOPException {
        for(int i = 0; i<1051; i++) {
            lookup(i, 90, 120);
        }
          
    }
}
