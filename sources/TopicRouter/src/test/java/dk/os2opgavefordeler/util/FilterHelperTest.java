package dk.os2opgavefordeler.util;

import dk.os2opgavefordeler.test.UnitTest;
import org.apache.deltaspike.core.api.projectstage.ProjectStage;
import org.apache.deltaspike.testcontrol.api.TestControl;
import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import java.util.List;

@Category(UnitTest.class)
@RunWith(CdiTestRunner.class)
@TestControl(projectStage = ProjectStage.Development.class)
public class FilterHelperTest {

	@Test
	public void testStringAsIntRangeList() {
		final String simpleDayStr = "1";
		final String expectedSimpleDayResult = "[1]";
		final List<Integer> simpleList = FilterHelper.stringAsIntRangeList(simpleDayStr);

		Assert.assertNotNull("The returned list is null", simpleList);
		Assert.assertTrue("List contains more or less elements than expected", simpleList.size() == 1);
		Assert.assertEquals("The returned result is not as expected", expectedSimpleDayResult, simpleList.toString());

		final String simpleDateRangeStr = "1-10";
		final String expectedSimpleDateRangeResult = "[1, 2, 3, 4, 5, 6, 7, 8, 9, 10]";
		final List<Integer> simpleDateRangeList = FilterHelper.stringAsIntRangeList(simpleDateRangeStr);

		Assert.assertNotNull("The returned list is null", simpleDateRangeList);
		Assert.assertTrue("List contains more or less elements than expected", simpleDateRangeList.size() == 10);
		Assert.assertEquals("The returned result is not as expected", expectedSimpleDateRangeResult, simpleDateRangeList.toString());

		final String simpleMixedDateRangeStr = "1,2-5";
		final String expectedSimpleMixedDateRangeResult = "[1, 2, 3, 4, 5]";
		final List<Integer> simpleMixedDateRangeList = FilterHelper.stringAsIntRangeList(simpleMixedDateRangeStr);

		Assert.assertNotNull("The returned list is null", simpleMixedDateRangeList);
		Assert.assertTrue("List contains more or less elements than expected", simpleMixedDateRangeList.size() == 5);
		Assert.assertEquals("The returned result is not as expected", expectedSimpleMixedDateRangeResult, simpleMixedDateRangeList.toString());

		final String complexMixedDateRangeStr = "1,3-6,10,22-24,30";
		final String expectedComplexMixedDateRangeResult = "[1, 3, 4, 5, 6, 10, 22, 23, 24, 30]";
		final List<Integer> complexMixedDateRangeList = FilterHelper.stringAsIntRangeList(complexMixedDateRangeStr);

		Assert.assertNotNull("The returned list is null", complexMixedDateRangeList);
		Assert.assertTrue("List contains more or less elements than expected", complexMixedDateRangeList.size() == 10);
		Assert.assertEquals("The returned result is not as expected", expectedComplexMixedDateRangeResult, complexMixedDateRangeList.toString());
	}

}
