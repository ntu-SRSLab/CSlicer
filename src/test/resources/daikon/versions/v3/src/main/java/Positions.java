class Positions
{
	public static int MANAGER = 0;
	public static int EMPLOYEE = 1;
	public static int DRIVER = 2; 
	public static int PROGRAMMER = 3;
	
	public static String getTitle(int number)
	{
		if(number == MANAGER)
		{
			return "Manager";
		}
		else if (number == EMPLOYEE)
		{
			return "Employee";
		}
		else if (number == DRIVER)
		{
			return "Driver";
		}
		else if (number == PROGRAMMER)
		{
			return "Programmer";
		}
		else
		{
			return "NULL";
		}
	}
}