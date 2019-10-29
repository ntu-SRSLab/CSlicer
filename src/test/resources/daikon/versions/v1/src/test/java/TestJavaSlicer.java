import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestJavaSlicer
{
	@Test
	public void test()
	{
		int[] people = {Positions.MANAGER, Positions.DRIVER, Positions.PROGRAMMER, Positions.EMPLOYEE};
		
		int total = 0;
		Management m = new Management();
		
		int salary = 0;
		
		//int peopleSum = m.SumPeople(people);/////
		//System.out.println("There are " + peopleSum + " people.");
		
		//int employeeSum = m.SumEmployees(people);/////
		//System.out.println("The number of employee is " + employeeSum);
		
		for(int i=0; i<people.length; i++)
		{
			salary = m.calculateSumSalary(12, people[i], 2000);
			
			if(salary == Management.WRONG)
			{
				System.out.println("Something wrong!");
			}
			else
			{
				total += salary;
				System.out.println(Positions.getTitle(people[i]) + ", You will get salary of " + salary);
				//m.saySomethingAboutSalary(salary);/////
			}
		}
		
		System.out.println("As a boss, you have to pay" + total);
		
		//m.printInfo();/////
		
		//assertEquals(640000,total);
		
	}
	
}
