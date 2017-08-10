# SmartFindViewById

该项目派生于早期的 [wangzailfm/GenerateFindViewById](https://github.com/wangzailfm/GenerateFindViewById), <br/>
本项目基本上重构了原项目的所有代码，使得在一定程度上可以更方便地进行后期扩展。并在原项目基础上新增了智能查找布局文件，优化了展示界面，增加了 **I18N** 国际化支持等功能。<br/>
同时也编写了多个可以在 idea plugin 项目中重用的 utils 方法。<br/>
在此也向原项目作者表示感谢。

## 演示
![演示](https://github.com/Khande/SmartFindViewById/raw/release/SmartFindViewById.gif)

## 安装
- 方法 1. 首先下载该项目中的 `SmartFindViewById.jar` 包，然后打开 Android Studio `Preferences` 菜单中的 `Plugins`，点击 `Install plugin from disk...`
选择刚下载的文件安装并重启 Android Studio 即可开始使用
- 方法 2. 打开 Android Studio `Preferences` 菜单中的 `Plugins`，点击 `Browse repositories...`, 然后搜索 `SmartFindViewById`，然后安装并重启 Android Studio 即可开始使用

## 说明
该插件可以依次通过
- 选中布局文件名，如选中代码段 `setContentView（R.layout.activity_main);` 中的 `activity_main`
- 检测当前光标所在行是否包含布局文件名（通过 **R.layout.** 前缀判断）
- `Activity` `setContentView(R.layout.xxx)` 方法布局文件参数
- `Fragment`(包括 support_v4 fragment) 中 `onCreateView` 方法中 `inflate(R.layout.xxx...` 布局参数
- `RecyclerViewAdapter` 中 `onCreateViewHolder` 方法中 `inflate(R.layout.xxx...` 布局参数
- 弹输入框提示手动输入<br/>

来逐一尝试获取目标布局文件名，优先级从高到低，一旦有一个方法获取到了有效的布局文件名，
就会展示对应布局文件的所有拥有 id 的 View 控件，然后可以选择生成对应的 `findViewById` 和 `onClick` 点击事件代码。


## 用法
1. 在一个 `Activity`, `Fragment`, `RecyclerViewAdapter`（包含直接或间接子类）甚至一个一般的类文件中，如果对应代码中已存在布局文件名，
可以对着类文件右键选择 `SmartFindViewById` 菜单（或是 `CMD + N`(macOS), Android Studio `Code` 菜单选择 `SmartFindViewById` ）,
这时就会自动查找布局文件，然后弹出一个展示目标布局文件下的所有有 id 的 View 控件列表，
如果你的布局文件不是写在 `setContentView(R.layout.xxx)` 等常见代码中，你可以手动选定目标布局文件名或将光标定位到目标布局文件名所在的代码行, 最后还可以手动输入。
2. 在弹出的 View 控件列表弹框中，默认选中还没有编写 `findViewById` 代码的 View 控件，当然也可以手动选中或取消。
3. 可以根据 View 控件是否有 `android:clickable=true` 属性或是 `Button` 类控件，自动勾选自动生成 `onClick` 点击事件代码。
4. 可以编辑 View 控件生成类成员变量的变量名，View 成员变量名生成默认规则类似于： `android:id="@+id/btn_submit_info"` 对应的成员变量名为 `mSubmitInfoBtn`
5. 对于 `Fragment` 等需要 `rootView.findViewById`, 可以勾选 RootView 选项，同时可以设置该 RootView 的变量名，默认为 `itemView`, 对于非 `Activity` 类该选项默认勾选
6. 点击确认生成

## License
```
Copyright 2017 Khande

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

	http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```