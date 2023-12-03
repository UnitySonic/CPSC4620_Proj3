package cpsc4620;

import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.util.Date;

import javax.swing.plaf.TreeUI;

/*
 * This file is where most of your code changes will occur You will write the code to retrieve
 * information from the database, or save information to the database
 * 
 * The class has several hard coded static variables used for the connection, you will need to
 * change those to your connection information
 * 
 * This class also has static string variables for pickup, delivery and dine-in. If your database
 * stores the strings differently (i.e "pick-up" vs "pickup") changing these static variables will
 * ensure that the comparison is checking for the right string in other places in the program. You
 * will also need to use these strings if you store this as boolean fields or an integer.
 * 
 * 
 */

/**
 * A utility class to help add and retrieve information from the database
 */

public final class DBNinja {
	private static Connection conn;

	// Change these variables to however you record dine-in, pick-up and delivery,
	// and sizes and crusts
	public final static String pickup = "pickup";
	public final static String delivery = "delivery";
	public final static String dine_in = "dinein";

	public final static String size_s = "Small";
	public final static String size_m = "Medium";
	public final static String size_l = "Large";
	public final static String size_xl = "XLarge";

	public final static String crust_thin = "Thin";
	public final static String crust_orig = "Original";
	public final static String crust_pan = "Pan";
	public final static String crust_gf = "Gluten-Free";

	private static boolean connect_to_db() throws SQLException, IOException {

		try {
			conn = DBConnector.make_connection();
			return true;
		} catch (SQLException e) {
			System.out.println("SQLEXCEPTION");
			return false;
		} catch (IOException e) {
			System.out.println("IOEXCEPTION");
			return false;
		}

	}

	public static void addOrder(Order o) throws SQLException, IOException {
		connect_to_db();
		/*
		 * add code to add the order to the DB. Remember that we're not just
		 * adding the order to the order DB table, but we're also recording
		 * the necessary data for the delivery, dinein, and pickup tables
		 * 
		 */

		// DO NOT FORGET TO CLOSE YOUR CONNECTION
		conn.close();
	}

	public static void addPizza(Pizza p) throws SQLException, IOException {
		connect_to_db();
		/*
		 * Add the code needed to insert the pizza into into the database.
		 * Keep in mind adding pizza discounts and toppings associated with the pizza,
		 * there are other methods below that may help with that process.
		 * 
		 */
		String sqlQuery = "SELECT MAX(PizzaID) AS MaxPizzaID FROM pizza";
		PreparedStatement ps = conn.prepareStatement(sqlQuery);
		ResultSet rs = ps.executeQuery();
		int nextPizzaID;

		if (rs.next())
			nextPizzaID = rs.getInt("MaxPizzaID") + 1;
		else
			nextPizzaID = 100;
		p.setPizzaID(nextPizzaID);

		// Prepare the SQL query for insertion
		sqlQuery = "INSERT INTO pizza (PizzaID, PizzaOrderID, PizzaSize, PizzaCrustType, PizzaPrice, PizzaCost, PizzaCurrentState) "
				+
				"VALUES (?, ?, ?, ?, ?, ?, ?)";

		ps = conn.prepareStatement(sqlQuery);
		ps.setInt(1, p.getPizzaID());
		ps.setInt(2, p.getOrderID());
		ps.setString(3, p.getSize());
		ps.setString(4, p.getCrustType());
		ps.setDouble(5, p.getCustPrice());
		ps.setDouble(6, p.getBusPrice());
		ps.setString(7, p.getPizzaState());
		ps.executeUpdate();

		conn.close();

		for (Topping t : p.getToppingsUsed().keySet()) {
			useTopping(p, t, p.getToppingsUsed().get(t));
		}

		for (Discount d : p.getDiscounts()) {
			usePizzaDiscount(p, d);
		}

		// DO NOT FORGET TO CLOSE YOUR CONNECTION
	}

	public static boolean checkIfEnoughTopping(int ToppingID, String size, boolean extraRequested)
			throws SQLException, IOException {
		connect_to_db();

		String sqlQuery = "SELECT ToppingCurrentInventory FROM topping WHERE ToppingID = ?";

		PreparedStatement ps = conn.prepareStatement(sqlQuery);
		ps.setInt(1, ToppingID);
		ResultSet resultSet = ps.executeQuery();
		resultSet.next();

		double currentInventory = resultSet.getDouble("ToppingCurrentInventory");

		double toppingUnits;
		String ColumnName = "";

		switch (size) {
			case "Small":
				sqlQuery = "SELECT ToppingSmallSizeUnits FROM topping WHERE ToppingID = ?";
				ColumnName = "ToppingSmallSizeUnits";
				break;
			case "Medium":
				sqlQuery = "SELECT ToppingMediumSizeUnits FROM topping WHERE ToppingID = ?";
				ColumnName = "ToppingMediumSizeUnits";
				break;
			case "Large":
				sqlQuery = "SELECT ToppingLargeSizeUnits FROM topping WHERE ToppingID = ?";
				ColumnName = "ToppingLargeSizeUnits";
				break;
			case "XLarge":
				sqlQuery = "SELECT ToppingXLargeSizeUnits FROM topping WHERE ToppingID = ?";
				ColumnName = "ToppingXLargeSizeUnits";
				break;
		}
		ps = conn.prepareStatement(sqlQuery);
		ps.setDouble(1, ToppingID);
		resultSet = ps.executeQuery();
		resultSet.next();

		toppingUnits = resultSet.getDouble(ColumnName);

		if (extraRequested)
			toppingUnits *= 2;

		// Query
		sqlQuery = "SELECT ToppingMinimumInventory FROM topping WHERE ToppingID = ?";
		ps = conn.prepareStatement(sqlQuery);
		ps.setDouble(1, ToppingID);
		resultSet = ps.executeQuery();
		resultSet.next();

		double minimumInventory = resultSet.getDouble("ToppingMinimumInventory");

		conn.close();

		return (currentInventory - toppingUnits >= minimumInventory);

	}

	public static void useTopping(Pizza p, Topping t, boolean isDoubled) throws SQLException, IOException {
		connect_to_db();
		/*
		 * This method should do 2 two things.
		 * - update the topping inventory every time we use t topping (accounting for
		 * extra toppings as well)
		 * - connect the topping to the pizza
		 * What that means will be specific to your implementatinon.
		 * 
		 * 
		 * Ideally, you shouldn't let toppings go negative....but this should be dealt
		 * with BEFORE calling this method.
		 * 
		 * 
		 */

		String sqlQuery = "INSERT INTO pizza_topping (Pizza_ToppingPizzaID, Pizza_ToppingToppingID, Pizza_ToppingExtraRequested) "
				+
				"VALUES (?, ?, ?)";
		PreparedStatement ps = conn.prepareStatement(sqlQuery);

		ps.setInt(1, p.getPizzaID());
		ps.setInt(2, t.getTopID());
		ps.setBoolean(3, isDoubled);
		ps.executeUpdate();

		double unitsToUse = t.getPerAMT();

		switch (p.getSize()) {
			case "Small":
				unitsToUse = t.getPerAMT();
				break;
			case "Medium":
				unitsToUse = t.getMedAMT();
				break;
			case "Large":
				unitsToUse = t.getLgAMT();
				break;
			case "X-Large":
				unitsToUse = t.getXLAMT();
		}
		if (isDoubled)
			unitsToUse *= 2;

		sqlQuery = "UPDATE topping SET ToppingCurrentInventory = ToppingCurrentInventory - ? WHERE ToppingID = ?";
		ps = conn.prepareStatement(sqlQuery);

		// Set the values for the parameters
		ps.setDouble(1, unitsToUse);
		ps.setInt(2, t.getTopID());
		ps.executeUpdate();
		conn.close();

		// DO NOT FORGET TO CLOSE YOUR CONNECTION
	}

	public static void usePizzaDiscount(Pizza p, Discount d) throws SQLException, IOException {
		connect_to_db();
		/*
		 * This method connects a discount with a Pizza in the database.
		 * 
		 * What that means will be specific to your implementatinon.
		 */

		String sqlQuery = "INSERT INTO pizza_discount (Pizza_DiscountDiscount_ID, Pizza_DiscountPizza_ID) "
				+
				"VALUES (?, ?)";
		PreparedStatement ps = conn.prepareStatement(sqlQuery);

		ps.setInt(1, d.getDiscountID());
		ps.setInt(2, p.getPizzaID());
		ps.executeUpdate();
		conn.close();

		// DO NOT FORGET TO CLOSE YOUR CONNECTION
	}

	public static void useOrderDiscount(Order o, Discount d) throws SQLException, IOException {
		connect_to_db();
		/*
		 * This method connects a discount with an order in the database
		 * 
		 * You might use this, you might not depending on where / how to want to update
		 * this information in the dabast
		 */

		// DO NOT FORGET TO CLOSE YOUR CONNECTION
	}

	public static void addCustomer(Customer c) throws SQLException, IOException {
		connect_to_db();
		/*
		 * This method adds a new customer to the database.
		 * 
		 */
		final int FIRST_CUST_ID = 1;
		// Get the number of customers from the database
		String count_query = "SELECT COUNT(*) FROM customer;";
		PreparedStatement ps = conn.prepareStatement(count_query);
		ResultSet resultSet = ps.executeQuery();
		int numCust = 0;
		while (resultSet.next()) {
			numCust = resultSet.getInt(1);
		}
		// Set the new customer's ID using the count
		c.setCustID(numCust + FIRST_CUST_ID);

		// Add the customer to the database with a prepared statement
		String insert_query = "INSERT INTO customer (CustomerFName, CustomerLName, CustomerPhoneNumber) VALUES(?, ?, ?);";
		ps = conn.prepareStatement(insert_query);
		ps.setString(1, c.getFName());
		ps.setString(2, c.getLName());
		ps.setString(3, c.getPhone());
		ps.executeUpdate();

		// DO NOT FORGET TO CLOSE YOUR CONNECTION
		conn.close();
	}

	public static void completeOrder(Order o) throws SQLException, IOException {
		connect_to_db();
		/*
		 * Find the specifed order in the database and mark that order as complete in
		 * the database.
		 * 
		 */

		// DO NOT FORGET TO CLOSE YOUR CONNECTION
	}

	public static ArrayList<Order> getOrders(boolean openOnly) throws SQLException, IOException {
		connect_to_db();
		/*
		 * Return an arraylist of all of the orders.
		 * openOnly == true => only return a list of open (ie orders that have not been
		 * marked as completed)
		 * == false => return a list of all the orders in the database
		 * Remember that in Java, we account for supertypes and subtypes
		 * which means that when we create an arrayList of orders, that really
		 * means we have an arrayList of dineinOrders, deliveryOrders, and pickupOrders.
		 * 
		 * Don't forget to order the data coming from the database appropriately.
		 * 
		 */

		// DO NOT FORGET TO CLOSE YOUR CONNECTION
		return null;
	}

	public static Order getLastOrder() {
		/*
		 * Query the database for the LAST order added
		 * then return an Order object for that order.
		 * NOTE...there should ALWAYS be a "last order"!
		 */

		return null;
	}

	public static ArrayList<Order> getOrdersByDate(String date) {
		/*
		 * Query the database for ALL the orders placed on a specific date
		 * and return a list of those orders.
		 * 
		 */

		return null;
	}

	public static ArrayList<Discount> getDiscountList() throws SQLException, IOException {
		connect_to_db();
		/*
		 * Query the database for all the available discounts and
		 * return them in an arrayList of discounts.
		 * 
		 */

		// DO NOT FORGET TO CLOSE YOUR CONNECTION
		String sqlQuery = "SELECT * FROM discount";

		PreparedStatement ps = conn.prepareStatement(sqlQuery);
		ResultSet resultSet = ps.executeQuery();

		boolean isPercent;

		ArrayList<Discount> DiscountList = new ArrayList<Discount>();

		while (resultSet.next()) {
			int DiscountID = resultSet.getInt("DiscountID");
			String DiscountName = resultSet.getString("DiscountName");

			double amountOff = resultSet.getDouble("DiscountDollarsOFF");

			if (resultSet.wasNull()) {
				isPercent = true;
				amountOff = resultSet.getDouble("DiscountPercent_Off");
			} else
				isPercent = false;
			Discount currentDiscount = new Discount(DiscountID, DiscountName, amountOff, isPercent);
			DiscountList.add(currentDiscount);
		}
		conn.close();
		return DiscountList;
	}

	public static void printDiscounts() throws SQLException, IOException {
		ArrayList<Discount> discountList = getDiscountList();

		for (Discount d : discountList) {
			System.out.println("DiscountID=" + Integer.toString(d.getDiscountID()) + " | "
					+ d.getDiscountName() + ", Amount= " + Double.toString(d.getAmount()) + ", isPercent= "
					+ Boolean.toString(d.isPercent()));
		}
	}

	public static Discount findDiscountByName(String name) throws SQLException, IOException {
		/*
		 * Query the database for a discount using its name.
		 * If found, then return an OrderDiscount object for the discount.
		 * If it's not found....then return null
		 * 
		 */
		connect_to_db();

		String sqlQuery = "SELECT * from discount WHERE DiscountName = " + name;

		PreparedStatement ps = conn.prepareStatement(sqlQuery);
		ResultSet rs = ps.executeQuery();

		if (rs.next()) {

			int DiscountID = rs.getInt("DiscountID");
			String DiscountName = rs.getString("DiscountName");
			double amountOff = rs.getDouble("DiscountDollarsOFF");
			boolean isPercent;

			if (rs.wasNull()) {
				isPercent = true;
				amountOff = rs.getDouble("DiscountPercent_Off");
			} else
				isPercent = false;

			Discount discount = new Discount(DiscountID, DiscountName, amountOff, isPercent);
			conn.close();
			return discount;
		} else {
			conn.close();
			return null;
		}

	}

	public static ArrayList<Customer> getCustomerList() throws SQLException, IOException {
		connect_to_db();
		/*
		 * Query the data for all the customers and return an arrayList of all the
		 * customers.
		 * Don't forget to order the data coming from the database appropriately.
		 * 
		 */

		ArrayList<Customer> customerList = new ArrayList<Customer>();
		Customer cust = null;
		// Query the database
		String query = "SELECT * FROM customer;";
		PreparedStatement ps = conn.prepareStatement(query);
		ResultSet resultSet = ps.executeQuery();
		// Get all customers from the table
		while (resultSet.next()) {
			// Add each customer to the list
			cust = new Customer(resultSet.getInt("CustomerID"), resultSet.getString("CustomerFName"),
					resultSet.getString("CustomerLName"), resultSet.getString("CustomerPhoneNumber"));
			cust.setAddress(resultSet.getString("CustomerStreetAddress"), resultSet.getString("CustomerCity"),
					resultSet.getString("CustomerState"), resultSet.getString("CustomerZipCode"));
			customerList.add(cust);
		}

		// DO NOT FORGET TO CLOSE YOUR CONNECTION
		conn.close();
		return customerList;
	}

	public static Customer findCustomerByPhone(String phoneNumber) throws SQLException, IOException {
		connect_to_db();

		/*
		 * Query the database for a customer using a phone number.
		 * If found, then return a Customer object for the customer.
		 * If it's not found....then return null
		 * 
		 */

		Customer cust = null;
		// Get the customer with the provided phone number
		String query = "SELECT * FROM customer WHERE CustomerPhoneNumber = ?;";
		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, phoneNumber);
		ResultSet resultSet = ps.executeQuery();
		while (resultSet.next()) {
			// If there is a match, create the customer using it
			cust = new Customer(resultSet.getInt("CustomerID"), resultSet.getString("CustomerFName"),
					resultSet.getString("CustomerLName"), resultSet.getString("CustomerPhoneNumber"));
			cust.setAddress(resultSet.getString("CustomerStreetAddress"), resultSet.getString("CustomerCity"),
					resultSet.getString("CustomerState"), resultSet.getString("CustomerZipCode"));
		}

		// DO NOT FORGET TO CLOSE YOUR CONNECTION
		conn.close();
		return cust;
	}

	public static ArrayList<Topping> getToppingList() throws SQLException, IOException {
		connect_to_db();
		/*
		 * Query the database for the aviable toppings and
		 * return an arrayList of all the available toppings.
		 * Don't forget to order the data coming from the database appropriately.
		 * 
		 */
		ArrayList<Topping> toppingList = new ArrayList<Topping>();
		Topping topping = null;
		// Query the database
		String query = "SELECT * FROM topping;";
		PreparedStatement ps = conn.prepareStatement(query);
		ResultSet resultSet = ps.executeQuery();
		// Get all customers from the table
		while(resultSet.next()) {
			// If a topping is found, create the object
			int toppingID = resultSet.getInt("ToppingID");
			String toppingName = resultSet.getString("ToppingName");
			double toppingPrice = resultSet.getDouble("ToppingPricePerUnit");
			double toppingCost = resultSet.getDouble("ToppingCostPerUnit");
			double currentInventory = resultSet.getDouble("ToppingCurrentInventory");
			double minimumInventory = resultSet.getDouble("ToppingMinimumInventory");
			double ToppingSmallSizeUnits = resultSet.getDouble("ToppingSmallSizeUnits");
			double ToppingMediumSizeUnits = resultSet.getDouble("ToppingMediumSizeUnits");
			double ToppingLargeSizeUnits = resultSet.getDouble("ToppingLargeSizeUnits");
			double ToppingXLargeSizeUnits = resultSet.getDouble("ToppingXLargeSizeUnits");

			topping = new Topping(toppingID, toppingName, ToppingSmallSizeUnits, ToppingMediumSizeUnits,
					ToppingLargeSizeUnits, ToppingXLargeSizeUnits, toppingPrice, toppingCost, minimumInventory,
					currentInventory);
			toppingList.add(topping);
		}

		// DO NOT FORGET TO CLOSE YOUR CONNECTION
		conn.close();
		return toppingList;
	}

	public static Topping findToppingByName(String name) throws SQLException, IOException {
		connect_to_db();
		/*
		 * Query the database for the topping using its name.
		 * If found, then return a Topping object for the topping.
		 * If it's not found....then return null
		 * 
		 */
		Topping topping = null;
		String query = "SELECT * FROM topping WHERE ToppingName = ?;";
		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, name);
		ResultSet resultSet = ps.executeQuery();

		while(resultSet.next()) {
			// If a topping is matched, create the object
			int toppingID = resultSet.getInt("ToppingID");
			String toppingName = resultSet.getString("ToppingName");
			double toppingPrice = resultSet.getDouble("ToppingPricePerUnit");
			double toppingCost = resultSet.getDouble("ToppingCostPerUnit");
			double currentInventory = resultSet.getDouble("ToppingCurrentInventory");
			double minimumInventory = resultSet.getDouble("ToppingMinimumInventory");
			double ToppingSmallSizeUnits = resultSet.getDouble("ToppingSmallSizeUnits");
			double ToppingMediumSizeUnits = resultSet.getDouble("ToppingMediumSizeUnits");
			double ToppingLargeSizeUnits = resultSet.getDouble("ToppingLargeSizeUnits");
			double ToppingXLargeSizeUnits = resultSet.getDouble("ToppingXLargeSizeUnits");

			topping = new Topping(toppingID, toppingName, ToppingSmallSizeUnits, ToppingMediumSizeUnits,
					ToppingLargeSizeUnits, ToppingXLargeSizeUnits, toppingPrice, toppingCost, minimumInventory,
					currentInventory);
		}

		conn.close();
		return topping;
	}

	public static Discount findDiscountByID(String ID) throws SQLException, IOException {
		connect_to_db();

		int discountIDToCheck;

		try {

			discountIDToCheck = Integer.parseInt(ID);
		} catch (Exception e) {
			return null;
		}
		String sqlQuery = "SELECT * FROM discount WHERE DiscountID = ?";
		PreparedStatement ps = conn.prepareStatement(sqlQuery);

		ps.setInt(1, discountIDToCheck);

		// Execute the query
		ResultSet rs = ps.executeQuery();

		// Check if any rows were returned
		if (rs.next()) {
			int DiscountID = rs.getInt("DiscountID");
			String DiscountName = rs.getString("DiscountName");
			double amountOff = rs.getDouble("DiscountDollarsOFF");
			boolean isPercent;

			if (rs.wasNull()) {
				isPercent = true;
				amountOff = rs.getDouble("DiscountPercent_Off");
			} else
				isPercent = false;

			Discount discount = new Discount(DiscountID, DiscountName, amountOff, isPercent);
			conn.close();
			return discount;
		} else {
			conn.close();
			return null;
		}

	}

	public static Topping findToppingByID(String ID) throws SQLException, IOException {
		connect_to_db();

		int toppingIDToCheck;

		try {
			toppingIDToCheck = Integer.parseInt(ID);
		} catch (Exception e) {
			return null;
		}

		String sqlQuery = "SELECT * FROM topping WHERE ToppingID = ?";
		PreparedStatement ps = conn.prepareStatement(sqlQuery);

		ps.setInt(1, toppingIDToCheck);

		// Execute the query
		ResultSet rs = ps.executeQuery();

		// Check if any rows were returned
		if (rs.next()) {

			int toppingID = rs.getInt("ToppingID");
			String toppingName = rs.getString("ToppingName");
			double toppingPrice = rs.getDouble("ToppingPricePerUnit");
			double toppingCost = rs.getDouble("ToppingCostPerUnit");
			double currentInventory = rs.getDouble("ToppingCurrentInventory");
			double minimumInventory = rs.getDouble("ToppingMinimumInventory");
			double ToppingSmallSizeUnits = rs.getDouble("ToppingSmallSizeUnits");
			double ToppingMediumSizeUnits = rs.getDouble("ToppingMediumSizeUnits");
			double ToppingLargeSizeUnits = rs.getDouble("ToppingLargeSizeUnits");
			double ToppingXLargeSizeUnits = rs.getDouble("ToppingXLargeSizeUnits");

			Topping topping = new Topping(toppingID, toppingName, ToppingSmallSizeUnits, ToppingMediumSizeUnits,
					ToppingLargeSizeUnits, ToppingXLargeSizeUnits, toppingPrice, toppingCost, minimumInventory,
					currentInventory);
			conn.close();
			return topping;
		} else {
			conn.close();
			return null;
		}

	}

	public static void addToInventory(Topping t, double quantity) throws SQLException, IOException {
		connect_to_db();
		/*
		 * Updates the quantity of the topping in the database by the amount specified.
		 * 
		 */
		String query = "UPDATE topping SET ToppingCurrentInventory = ? WHERE ToppingID = ?;";
		PreparedStatement ps = conn.prepareStatement(query);
		ps.setDouble(1, t.getCurINVT()+quantity);
		ps.setInt(2, t.getTopID());
		ps.executeUpdate();

		// DO NOT FORGET TO CLOSE YOUR CONNECTION
		conn.close();
	}

	public static double getBaseCustPrice(String size, String crust) throws SQLException, IOException {
		connect_to_db();

		/*
		 * Query the database for the base customer price for that size and crust pizza.
		 * 
		 */
		double baseCustPrice = 0.0;
		String query = "SELECT Base_Price_And_CostBasePrice FROM base_price_and_cost " +
				"WHERE Base_Price_And_CostPizzaSize = ? AND Base_Price_And_CostCrustType = ?;";
		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, size);
		ps.setString(2, crust);
		ResultSet resultSet = ps.executeQuery();
		while (resultSet.next()) {
			baseCustPrice = resultSet.getDouble("Base_Price_And_CostBasePrice");
		}

		// DO NOT FORGET TO CLOSE YOUR CONNECTION
		conn.close();
		return baseCustPrice;

	}

	public static double getBaseBusPrice(String size, String crust) throws SQLException, IOException {
		connect_to_db();
		/*
		 * 
		 * Query the database for the base business price for that size and crust pizza.
		 *
		 */
		double baseBusPrice = 0.0;
		String query = "SELECT Base_Price_And_CostBaseCost FROM base_price_and_cost " +
				"WHERE Base_Price_And_CostPizzaSize = ? AND Base_Price_And_CostCrustType = ?;";
		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, size);
		ps.setString(2, crust);
		ResultSet resultSet = ps.executeQuery();
		while (resultSet.next()) {
			baseBusPrice = resultSet.getDouble("Base_Price_And_CostBaseCost");
		}

		// DO NOT FORGET TO CLOSE YOUR CONNECTION
		conn.close();
		return baseBusPrice;

	}

	public static void printInventory() throws SQLException, IOException {
		connect_to_db();
		/*
		 * Queries the database and prints the current topping list with quantities.
		 * 
		 * The result should be readable and sorted as indicated in the prompt.
		 * 
		 */

		// SQL query to retrieve ToppingID, ToppingName, and CurrentInventory columns
		String sqlQuery = "SELECT ToppingID, ToppingName, ToppingCurrentInventory FROM topping ORDER BY ToppingID";

		try {
			// Create a PreparedStatement
			PreparedStatement preparedStatement = conn.prepareStatement(sqlQuery);

			// Execute the query and obtain the ResultSet
			ResultSet resultSet = preparedStatement.executeQuery();

			System.out.println("ID\tName\tCurINVT");

			// Iterate through the ResultSet and print each row
			while (resultSet.next()) {
				int toppingID = resultSet.getInt("ToppingID");
				String toppingName = resultSet.getString("ToppingName");
				int currentInventory = resultSet.getInt("ToppingCurrentInventory");

				System.out.println(toppingID + "\t" + toppingName + "\t" + currentInventory);
			}

			// Close resources
			resultSet.close();
			preparedStatement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			// Close the connection in the finally block to ensure it's always closed, even
			// if an exception occurs
			try {
				if (conn != null && !conn.isClosed()) {
					conn.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	// DO NOT FORGET TO CLOSE YOUR CONNECTION

	public static void printToppingPopReport() throws SQLException, IOException {
		connect_to_db();
		/*
		 * Prints the ToppingPopularity view. Remember that this view
		 * needs to exist in your DB, so be sure you've run your createViews.sql
		 * files on your testing DB if you haven't already.
		 * 
		 * The result should be readable and sorted as indicated in the prompt.
		 * 
		 */
		String query = "SELECT * FROM ToppingPopularity;";
		PreparedStatement ps = conn.prepareStatement(query);
		ResultSet resultSet = ps.executeQuery();

		System.out.println("Topping\tToppingCount");
		while (resultSet.next()) {
			System.out.println(resultSet.getString("Topping") + "\t" + resultSet.getString("ToppingCount"));
		}

		// DO NOT FORGET TO CLOSE YOUR CONNECTION
		conn.close();

	}

	public static void printProfitByPizzaReport() throws SQLException, IOException {
		connect_to_db();
		/*
		 * Prints the ProfitByPizza view. Remember that this view
		 * needs to exist in your DB, so be sure you've run your createViews.sql
		 * files on your testing DB if you haven't already.
		 * 
		 * The result should be readable and sorted as indicated in the prompt.
		 * 
		 */

		String query = "SELECT * FROM ProfitByPizza;";
		PreparedStatement ps = conn.prepareStatement(query);
		ResultSet resultSet = ps.executeQuery();

		System.out.println("Pizza Size\tPizza Crust\tProfit\tLastOrderDate");
		while (resultSet.next()) {
			System.out.println(resultSet.getString("Size") + "\t" + resultSet.getString("Crust") +
					"\t" + resultSet.getString("Profit") + "\t" + resultSet.getString("OrderMonth"));
		}

		// DO NOT FORGET TO CLOSE YOUR CONNECTION
		conn.close();
	}

	public static void printProfitByOrderType() throws SQLException, IOException {

		connect_to_db();
		/*
		 * Prints the ProfitByOrderType view. Remember that this view
		 * needs to exist in your DB, so be sure you've run your createViews.sql
		 * files on your testing DB if you haven't already.
		 * 
		 * The result should be readable and sorted as indicated in the prompt.
		 * 
		 */

		String query = "SELECT * FROM ProfitByOrderType;";
		PreparedStatement ps = conn.prepareStatement(query);
		ResultSet resultSet = ps.executeQuery();

		System.out.println("OrderType\tOrder Month\tTotalOrderPrice\tTotalOrderCost\tProfit");
		while (resultSet.next()) {
			System.out.println(resultSet.getString("customerType") + "\t" +
					resultSet.getString("OrderMonth") + "\t" + resultSet.getString("TotalOrderPrice")
					+ "\t" + resultSet.getString("TotalOrderCost") + "\t" + resultSet.getString("Profit"));
		}

		// DO NOT FORGET TO CLOSE YOUR CONNECTION
		conn.close();
	}

	public static String getCustomerName(int CustID) throws SQLException, IOException {
		/*
		 * 
		 * This is a helper method to fetch and format the name of a customer
		 * based on a customer ID. This is an example of how to interact with
		 * your database from Java. It's used in the model solution for this
		 * project...so the code works!
		 * 
		 * OF COURSE....this code would only work in your application if the table &
		 * field names match!
		 *
		 */

		connect_to_db();

		/*
		 * an example query using a constructed string...
		 * remember, this style of query construction could be subject to sql injection
		 * attacks!
		 * 
		 */
		String cname1 = "";
		String query = "Select CustomerFName, CustomerLName From customer WHERE CustomerID=" + CustID + ";";
		Statement stmt = conn.createStatement();
		ResultSet rset = stmt.executeQuery(query);

		while (rset.next()) {
			cname1 = rset.getString(1) + " " + rset.getString(2);
		}

		/*
		 * an example of the same query using a prepared statement...
		 * 
		 */
		String cname2 = "";
		PreparedStatement os;
		ResultSet rset2;
		String query2;
		query2 = "Select CustomerFName, CustomerLName From customer WHERE CustomerID=?;";
		os = conn.prepareStatement(query2);
		os.setInt(1, CustID);
		rset2 = os.executeQuery();
		while (rset2.next()) {
			cname2 = rset2.getString("CustomerFName") + " " + rset2.getString("CustomerLName"); // note the use of field
																								// names in the getSting
																								// methods
		}

		conn.close();
		return cname2; // OR cname2
	}

	/*
	 * The next 3 private methods help get the individual components of a SQL
	 * datetime object.
	 * You're welcome to keep them or remove them.
	 */
	private static int getYear(String date)// assumes date format 'YYYY-MM-DD HH:mm:ss'
	{
		return Integer.parseInt(date.substring(0, 4));
	}

	private static int getMonth(String date)// assumes date format 'YYYY-MM-DD HH:mm:ss'
	{
		return Integer.parseInt(date.substring(5, 7));
	}

	private static int getDay(String date)// assumes date format 'YYYY-MM-DD HH:mm:ss'
	{
		return Integer.parseInt(date.substring(8, 10));
	}

	public static boolean checkDate(int year, int month, int day, String dateOfOrder) {
		if (getYear(dateOfOrder) > year)
			return true;
		else if (getYear(dateOfOrder) < year)
			return false;
		else {
			if (getMonth(dateOfOrder) > month)
				return true;
			else if (getMonth(dateOfOrder) < month)
				return false;
			else {
				if (getDay(dateOfOrder) >= day)
					return true;
				else
					return false;
			}
		}
	}
}