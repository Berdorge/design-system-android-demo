package cases;

import static org.assertj.core.api.Assertions.assertThat;

import android.content.Context;
import android.util.TypedValue;

import com.vkui.analysis.runtime.DesignAnalysisMark;
import com.vkui.analysis.runtime.DesignAnalysisTracker;
import com.vkui.analysis.test.R;

public class DoubleReturnAttribute {
    public void test(Context context) {
        returnAttribute(context);
        DesignAnalysisMark returnValueMark = DesignAnalysisTracker.getReturnValue();
        assertThat(returnValueMark).isNotNull();
        assertThat(returnValueMark.getDescription()).startsWith("attribute");
    }

    private static double returnAttribute(Context context) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.test_attr, typedValue, true);
        return typedValue.data;
    }
}
