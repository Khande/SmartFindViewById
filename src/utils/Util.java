package utils;

public class Util {

    /**
     * 创建onCreate方法
     *
     * @param mSelectedText
     * @return
     */
    public static String createOnCreateMethod(String mSelectedText) {
        StringBuilder method = new StringBuilder();
        method.append("@Override protected void onCreate(android.os.Bundle savedInstanceState) {\n");
        method.append("super.onCreate(savedInstanceState);\n");
        method.append("\t// TODO:OnCreate Method has been created, run FindViewById again to generate code\n");
        method.append("\tsetContentView(R.layout.");
        method.append(mSelectedText);
        method.append(");\n");
        method.append("}");
        return method.toString();
    }

    /**
     * 创建onCreateView方法
     *
     * @param mSelectedText
     * @return
     */
    public static String createOnCreateViewMethod(String mSelectedText) {
        StringBuilder method = new StringBuilder();
        method.append("@Override public view onCreateView(android.view.LayoutInflater inflater, android.view.ViewGroup container, android.os.Bundle savedInstanceState) {\n");
        method.append("\t// TODO:OnCreateView Method has been created, run FindViewById again to generate code\n");
        method.append("\tview view = view.inflate(getActivity(), R.layout.");
        method.append(mSelectedText);
        method.append(", null);");
        method.append("return view;");
        method.append("}");
        return method.toString();
    }
}
