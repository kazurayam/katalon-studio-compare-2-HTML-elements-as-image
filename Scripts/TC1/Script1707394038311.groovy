import java.awt.Color
import java.awt.image.BufferedImage
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import javax.imageio.ImageIO

import org.apache.commons.io.FileUtils

import com.kms.katalon.core.configuration.RunConfiguration
import com.kms.katalon.core.testobject.ConditionType
import com.kms.katalon.core.testobject.TestObject
import com.kms.katalon.core.util.KeywordUtil
import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords as WebUI

import ru.yandex.qatools.ashot.comparison.ImageDiff
import ru.yandex.qatools.ashot.comparison.ImageDiffer
import ru.yandex.qatools.ashot.comparison.ImageMarkupPolicy;

// if the difference of 2 images are greater than the criteria, then this test will fail.
// if the difference is geater than zero abut smaller than the criteria, then this test will warn you but will not fail.
double criteriaPercent = 10.0

// the directory to write image files into
Path docsDir = Paths.get(RunConfiguration.getProjectDir()).resolve("docs");
if (Files.exists(docsDir)) {
	FileUtils.deleteDirectory(docsDir.toFile())
}
Files.createDirectories(docsDir)

Path diff = docsDir.resolve("images/diff.png")
Path img1 = docsDir.resolve("images/img1.png")
Path img4 = docsDir.resolve("images/img4.png")

// Shall we start?
WebUI.openBrowser('')
WebUI.setViewPortSize(800, 600)

// visit a URL 
WebUI.navigateToUrl("https://kazurayam.github.io/myApple/page1.html")
// take screenshot of an HTML element, write the PNG image into a file
WebUI.takeElementScreenshot(img1.toString(), makeTestObject("img1", "//img[@id='apple']"))

// visit another URL
WebUI.navigateToUrl("https://kazurayam.github.io/myApple/page4.html")
// take screenshot of an HTML element, write the PNG image into a file
WebUI.takeElementScreenshot(img4.toString(), makeTestObject("img4", "//img[@id='apple']"))

// compare 2 PNG imgages to make a diff image, write the image into a file
ImageDiff imageDiff = makeDiff(img1, img4, diff)

// check the comparison result and report it
if (imageDiff.hasDiff()) {
	double diffRatio = calculateDiffRatioPercent(imageDiff)
	String diffRatioStr = String.format("%.2f", diffRatio)
	String msg = "The two images are different, the magnitue of difference is ${diffRatioStr}%"
	if (0 <= diffRatio && diffRatio < criteriaPercent) {
		KeywordUtil.markWarning(msg)
	} else if (criteriaPercent <= diffRatio && diffRatio <= 100.0) {
		KeywordUtil.markFailed(msg)
	} else {
		KeywordUtil.markErrorAndStop(msg)
	}
}

// done
WebUI.closeBrowser()

Path html = docsDir.resolve("compare.html")
createHTML(img1, img4, html)

/*
 * create a TestObject
 */
TestObject makeTestObject(String id, String xpath) {
	TestObject tObj = new TestObject(id)
	tObj.addProperty("xpath", ConditionType.EQUALS, xpath)
	return tObj
}

/*
 * drive the AShot library to compare 2 image files to compare them 
 * and make a diff image. will write the diff into the out file
 */
ImageDiff makeDiff(Path png1, Path png2, Path out) {
	BufferedImage bi1 = ImageIO.read(png1.toFile())
	BufferedImage bi2 = ImageIO.read(png2.toFile())
	ImageDiffer differ =
			new ImageDiffer()
			.withDiffMarkupPolicy(new ImageMarkupPolicy().withDiffColor(Color.GRAY))
	ImageDiff diff = differ.makeDiff(bi1, bi2);
	ImageIO.write(diff.getMarkedImage(), "png", out.toFile())
	return diff;
}

/*
 * Calculate the ratio of the different area against the whole page area in percentage
 *
 * e.g. 41.983224 -> 2 input images are different for 41% of pixels
 */
Double calculateDiffRatioPercent(ImageDiff diff) {
	boolean hasDiff = diff.hasDiff();
	if (!hasDiff) {
		return 0.0;
	}
	int diffSize = diff.getDiffSize();
	int area = diff.getMarkedImage().getWidth() * diff.getMarkedImage().getHeight();
	return diffSize * 1.0D / area * 100;
}

def createHTML(Path before, Path after, Path html) {
	StringBuilder sb = new StringBuilder()
	sb.append("""<!DOCTYPE html>
<html lang="ja" xml:lang="ja" xmlns="http://www.w3.org/1999/xhtml">
<head>
	<meta charset="utf-8" />
	<meta name="viewport" content="width=device-width, initial-scale=1" />
	<meta name="keywords" content="" />
	<meta name="description" content="" />
	<title>Compare Images Viewer example</title>
	<!-- https://image-compare-viewer.netlify.app/ -->
    <link rel="stylesheet" href="https://unpkg.com/image-compare-viewer/dist/image-compare-viewer.min.css" />
	<style>body { background-color: #ccc; #image-compare: width: 200px; height: 200px; margin:100px; }</style>
</head>
<body>
	<!--
    <header>
		<nav>
			<ul>
				<li></li>
			</ul>
		</nav>
	</header>
    -->
	<main>
		<article>
			<section>
				<h2>Compare Image Viewer compares two overlaid images interactively with intuitive slider controls</h2>
                <div id="image-compare">
                    <img src="images/img1.png" alt="" />
                    <img src="images/img4.png" alt="" />
                </div>
            </section>
		</article>
	</main>
	<footer>
	</footer>
	<!-- https://image-compare-viewer.netlify.app/ -->
	<script src="https://unpkg.com/image-compare-viewer/dist/image-compare-viewer.min.js"></script>
	<script>
const element = document.getElementById("image-compare");
const viewer = new ImageCompare(element).mount();

    </script>
</body>
</html>
	""")
	html.text = sb.toString()
}
