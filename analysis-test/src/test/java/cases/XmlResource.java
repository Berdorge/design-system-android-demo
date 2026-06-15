package cases;

import static org.assertj.core.api.Assertions.assertThat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import com.vkui.analysis.runtime.DesignAnalysisHelper;
import com.vkui.analysis.runtime.DesignAnalysisMark;
import com.vkui.analysis.runtime.shadow.DesignAnalysisViewProperties;
import com.vkui.analysis.test.R;

public class XmlResource {
    public void test(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.test_textview, null);
        DesignAnalysisViewProperties properties = DesignAnalysisHelper.getPropertiesOf(view);
        assertThat(properties).isNotNull();
        DesignAnalysisMark paddingEndMark = properties.getPaddingEnd();
        assertThat(paddingEndMark).isNotNull();
        assertThat(paddingEndMark.getDescription()).startsWith("resource");
    }
}
