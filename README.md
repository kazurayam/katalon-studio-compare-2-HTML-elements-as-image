# [katalon studio] How to use the AShot library to compare HTML elements visually

This is a Katalon Studio project for demonstration purpose.

This projects demostrates how to use the [AShot](https://mvnrepository.com/artifact/ru.yandex.qatools.ashot/ashot/1.5.4) library.

You need to download the jar of AShot from
- https://mvnrepository.com/artifact/ru.yandex.qatools.ashot/ashot/1.5.4
and save it into the `Drivers` folder of your Katalon project.

The Test Case `TC1` does the following stuff.

1. open a URL "https://kazurayam.github.io/myApple/page1.html" and take screenshot of the `<img>` element in it, save it into a file. The PNG will look like this:

![page1](https://kazurayam.github.io/katalon-studio-compare-2-HTML-elements-as-image/images/img1.png)

2. open another URL "https://kazurayam.github.io/myApple/page4.html" and take screenshot of the `<img>` element in it, save it into a file. The PNG will look like this:

![page4](https://kazurayam.github.io/katalon-studio-compare-2-HTML-elements-as-image/images/img4.png)

3. Use the AShot library to compare the 2 PNG files, generate a "diff image", write it into "out/diff.png". The "diff image" will look like this:

![diff](https://kazurayam.github.io/katalon-studio-compare-2-HTML-elements-as-image/images/diff.png)

4. The Test Case will fail because the 2 images are significantly different.

The `TC1` demonstrates how to use the AShot to compare 2 HTML elements. Please read the source code for more detail. Taking this code as the starting point, you should be able to develop tests that meet your complex conditions you want.
