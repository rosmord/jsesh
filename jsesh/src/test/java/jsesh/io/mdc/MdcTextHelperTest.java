package jsesh.io.mdc;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class MdcTextHelperTest {

    @Test
    public void testNormalize() throws Exception {
		String mdc= "G17-A1-nn-nTrw-R-N-i-w-r:a-O-$r-ra-[[-m-]]-p*t:pt-$b-10";
        String expected = "G17-A1-M22B-R8A-D153-S3-M17-G43-D21:D36-O-$r-N5-[[-G17-]]-Q3*X1:N1-$b-V20";
		String mdcNorm= MdcTextHelper.normalize(mdc);
        assertEquals(expected, mdcNorm);
	}
}

