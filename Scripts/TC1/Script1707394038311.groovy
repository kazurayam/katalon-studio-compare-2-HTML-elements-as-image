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
Path outDir = Paths.get(RunConfiguration.getProjectDir()).resolve("out");
if (Files.exists(outDir)) {
	FileUtils.deleteDirectory(outDir.toFile())
}
Files.createDirectories(outDir)

Path diff = outDir.resolve("diff.png")
Path img1 = outDir.resolve("img1.png")
Path img4 = outDir.resolve("img4.png")

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

