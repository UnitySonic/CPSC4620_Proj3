package cpsc4620;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.HashMap;
import java.util.Map;
import java.text.SimpleDateFormat;
import java.sql.Timestamp;

import static cpsc4620.DBNinja.*;

/*
 * This file is where the front end magic happens.
 * 
 * You will have to write the methods for each of the menu options.
 * 
 * This file should not need to access your DB at all, it should make calls to the DBNinja that will do all the connections.
 * 
 * You can add and remove methods as you see necessary. But you MUST have all of the menu methods (including exit!)
 * 
 * Simply removing menu methods because you don't know how to implement it will result in a major error penalty (akin to your program crashing)
 * 
 * Speaking of crashing. Your program shouldn't do it. Use exceptions, or if statements, or whatever it is you need to do to keep your program from breaking.
 * 
 */

public class Menu {

	public static BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

	public static void main(String[] args) throws SQLException, IOException {
		System.out.println("Welcome to Pizzas-R-Us!");

		int menu_option = 0;

		// present a menu of options and take their selection

		PrintMenu();
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		String option = reader.readLine();
		try {
			menu_option = Integer.parseInt(option);
		}
		catch (Exception ignored) {

		}

		while (menu_option != 9) {
			switch (menu_option) {
				case 1:// enter order
					EnterOrder();
					break;
				case 2:// view customers
					viewCustomers();
					break;
				case 3:// enter customer
					EnterCustomer();
					break;
				case 4:// view order
						// open/closed/date
					ViewOrders();
					break;
				case 5:// mark order as complete
					MarkOrderAsComplete();
					break;
				case 6:// view inventory levels
					ViewInventoryLevels();
					break;
				case 7:// add to inventory
					AddInventory();
					break;
				case 8:// view reports
					PrintReports();
					break;
			}
			PrintMenu();
			option = reader.readLine();
			try {
				menu_option = Integer.parseInt(option);
			}
			catch (Exception ignored) {

			}
		}
	}

	// Helper function that creates and returns a timestamp of the current date and time
	public static String createTimestamp() {
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return dateFormat.format(new Date(timestamp.getTime()));
	}

	// Helper method that returns true if a discount with a given ID is in a list. Otherwise, returns false
	public static boolean isDiscountInList(ArrayList<Discount> discountList, String ID) {
		int searchFor;
		try {
			searchFor = Integer.parseInt(ID);
		}
		catch(Exception ignored) {
			return false;
		}

		for(Discount d : discountList) {
			if(d.getDiscountID() == searchFor) {
				return true;
			}
		}
		return false;
	}

	// allow for a new order to be placed
	public static void EnterOrder() throws SQLException, IOException {
		/*
		 * EnterOrder should do the following:
		 * 
		 * Ask if the order is delivery, pickup, or dinein
		 * if dine in....ask for table number
		 * if pickup...
		 * if delivery...
		 * 
		 * Then, build the pizza(s) for the order (there's a method for this)
		 * until there are no more pizzas for the order
		 * add the pizzas to the order
		 *
		 * Apply order discounts as needed (including to the DB)
		 * 
		 * return to menu
		 * 
		 * make sure you use the prompts below in the correct order!
		 */
		ArrayList<Discount> discountList = new ArrayList<Discount>();

		// Order object fields
		Order newOrder;
		int order_ID = DBNinja.getNewOrderID();
		final int INVALID_ID = -99;
		int customer_ID = INVALID_ID;
		int table_num = 0;
		final int INSTORE_ID = 1;

		// Customer address fields
		String customer_street = "";
		String customer_city = "";
		String customer_state = "";
		String customer_zip = "";

		// User Input Prompts...
		System.out.println(
				"Is this order for: \n1.) Dine-in\n2.) Pick-up\n3.) Delivery\nEnter the number of your choice:");
		String option = reader.readLine();
		int order_type_choice = 0;
		try {
			order_type_choice = Integer.parseInt(option);
		}
		catch(Exception ignored) {

		}
		// Verify that user entered a valid choice
		while (order_type_choice < 1 || order_type_choice > 3) {
			System.out.println("Invalid choice. Please enter a number from 1 to 3.");
			System.out.println(
					"Is this order for: \n1.) Dine-in\n2.) Pick-up\n3.) Delivery\nEnter the number of your choice:");
			option = reader.readLine();
			try {
				order_type_choice = Integer.parseInt(option);
			}
			catch(Exception ignored) {

			}
		}
		// Set order type
		if (order_type_choice == 1) {
			// Dine-in orders need a table number
			System.out.println("What is the table number for this order?");
			option = reader.readLine();
			try {
				table_num = Integer.parseInt(option);
			}
			catch(Exception ignored) {

			}
			while (table_num <= 0) {
				System.out.println("Table number must be at least 1.");
				System.out.println("What is the table number for this order?");
				option = reader.readLine();
				try {
					table_num = Integer.parseInt(option);
				}
				catch(Exception ignored) {

				}
			}

			// Get a timestamp for the order
			String timestamp = createTimestamp();

			// Create a dine-in order
			newOrder = new DineinOrder(order_ID, INSTORE_ID, timestamp, 0.0, 0.0, 0, table_num);
		} else {
			// Pickup and delivery orders need a customer
			System.out.println("Is this order for an existing customer? Answer y/n: ");
			option = reader.readLine();
			while (!option.equals("y") && !option.equals("n")) {
				System.out.println("ERROR: I don't understand your input for: Is this order an existing customer?");
				System.out.println("Is this order for an existing customer? Answer y/n: ");
				option = reader.readLine();
			}
			if (option.equals("y")) {
				// Show the current customers
				System.out.println("Here's a list of the current customers: ");
				viewCustomers();
				do {
					System.out.println("Which customer is this order for? Enter ID Number:");
					option = reader.readLine();
					try {
						customer_ID = Integer.parseInt(option);
					}
					catch (Exception ignored) {
						// Ignore non-numeric customer IDs
						customer_ID = INVALID_ID;
					}
					// Check that the customer ID is valid. Query the ID in the database to make sure it exists.
				} while(customer_ID == INVALID_ID || DBNinja.getCustomerName(customer_ID).equals(""));
			} else {
				// Set the new customer's ID
				customer_ID = DBNinja.getNewCustomerID();
				// Prompt user to create a new customer
				EnterCustomer();
			}
			if (order_type_choice == 2) {
				String timestamp = createTimestamp();
				// Create a pick-up order
				newOrder = new PickupOrder(DBNinja.getNewOrderID(), customer_ID, timestamp, 0.0, 0.0, 0, 0);
			} else {
				// Delivery orders require a customer's address
				System.out.println("What is the House/Apt Number for this order? (e.g., 111)");
				customer_street = (reader.readLine() + " ");
				System.out.println("What is the Street for this order? (e.g., Smile Street)");
				customer_street += reader.readLine();
				System.out.println("What is the City for this order? (e.g., Greenville)");
				customer_city = reader.readLine();

				System.out.println("What is the State for this order? (e.g., SC)");
				customer_state = reader.readLine();
				Pattern check_state = Pattern.compile("^[A-Z]{2}$");
				Matcher match_state = check_state.matcher(customer_state);
				while(!match_state.find()) {
					System.out.println("What is the State for this order? (e.g., SC)");
					customer_state = reader.readLine();
					match_state = check_state.matcher(customer_state);
				}

				System.out.println("What is the Zip Code for this order? (e.g., 20605)");
				customer_zip = reader.readLine();
				Pattern check_zip = Pattern.compile("^[0-9]{5}$", Pattern.CASE_INSENSITIVE);
				Matcher match_zip = check_zip.matcher(customer_zip);
				while(!match_zip.find()) {
					System.out.println("What is the Zip Code for this order? (e.g., 20605)");
					customer_zip = reader.readLine();
					match_zip = check_zip.matcher(customer_zip);
				}

				String address = customer_street + " " + customer_city + " " + customer_state + " " + customer_zip;
				String timestamp = createTimestamp();

				// Update the customer's address in the database
				DBNinja.updateCustomerAddress(customer_ID, customer_street, customer_city, customer_state, customer_zip);

				// Create a delivery order
				newOrder = new DeliveryOrder(DBNinja.getNewOrderID(), customer_ID, timestamp, 0.0, 0.0, 0, address);
			}
		}

		// Prompt user to build a pizza
		System.out.println("Let's build a pizza!");
		Pizza newPizza;
		while (!option.equals("-1")) {
			newPizza = buildPizza(order_ID);
			newOrder.addPizza(newPizza);
			System.out.println(
					"Enter -1 to stop adding pizzas...Enter anything else to continue adding pizzas to the order.");
			option = reader.readLine();
		}

		// Prompt user to add discounts to the order
		System.out.println("Do you want to add discounts to this order? Enter y/n?");
		option = reader.readLine();
		while(!option.equals("y") && !option.equals("n")) {
			System.out.println("Do you want to add discounts to this order? Enter y/n?");
			option = reader.readLine();
		}

		Discount discount;
		if(option.equals("y")) {
			// Add discounts to the order
			option = "0";
			while (!option.equals("-1")) {
				DBNinja.printDiscounts();
				System.out.println(
						"Which Order Discount do you want to add? Enter the DiscountID. Enter -1 to stop adding Discounts: ");
				option = reader.readLine();

				// Query the discount table using the provided ID
				discount = DBNinja.findDiscountByID(option);
				if (discount != null) {
					// Add unique discounts to the order. Ensure that a discount does not drop the price below 0.
					if(isDiscountInList(discountList, option)) {
						System.out.println("This discount has already been applied.");
					}
					else if(!discount.isPercent() && (newOrder.getCustPrice() - discount.getAmount() < 0)) {
						System.out.println("Cannot add a discount that drops the price below 0.");
					}
					else {
						discountList.add(discount);
					}
				}
				else {
					if(!option.equals("-1"))
						System.out.println("This discount was not found.");
				}
			}
		}

		for(Discount d : discountList) {
			newOrder.addDiscount(d);
		}

		for(Pizza pizza : newOrder.getPizzaList()) {
			// Total up the prices of each pizza to get the order's prices
			newOrder.setBusPrice(newOrder.getBusPrice() + pizza.getBusPrice());
			newOrder.setCustPrice(newOrder.getCustPrice() + pizza.getCustPrice());
		}

		DBNinja.addOrder(newOrder);

		for(Pizza pizza : newOrder.getPizzaList()) {
			// Add all pizzas for the order to the database
			DBNinja.addPizza(pizza);
		}
		System.out.println("Finished adding order...Returning to menu...");
	}

	public static void viewCustomers() throws SQLException, IOException {
		/*
		 * Simply print out all of the customers from the database.
		 */
		ArrayList<Customer> customerList = DBNinja.getCustomerList();
		for (Customer customer : customerList) {
			System.out.println(customer.toString());
		}
	}

	// Enter a new customer in the database
	public static void EnterCustomer() throws SQLException, IOException {
		/*
		 * Ask for the name of the customer:
		 * First Name <space> Last Name
		 * 
		 * Ask for the phone number.
		 * (##########) (No dash/space)
		 * 
		 * Once you get the name and phone number, add it to the DB
		 */

		// Customer object fields
		String name = "";
		String phone = "";

		// User Input Prompts...
		System.out.println("Please Enter the Customer name (First Name <space> Last Name):");
		name = reader.readLine();
		// Use a regex pattern to verify the name is correct
		Pattern check_name = Pattern.compile("^[^\\s]+\\s[^\\s]+$", Pattern.CASE_INSENSITIVE);
		Matcher match_name = check_name.matcher(name);
		// Do not accept invalid names
		while (!match_name.find()) {
			System.out.println("Please Enter the Customer name (First Name <space> Last Name):");
			name = reader.readLine();
			match_name = check_name.matcher(name);
		}

		System.out.println("What is this customer's phone number (##########) (No dash/space)");
		phone = reader.readLine();
		// Use a regex pattern to verify the phone number is correct
		Pattern check_phone = Pattern.compile("^[0-9]{10}$", Pattern.CASE_INSENSITIVE);
		Matcher match_phone = check_phone.matcher(phone);
		// Do not accept invalid phone numbers
		while (!match_phone.find()) {
			System.out.println("What is this customer's phone number (##########) (No dash/space)");
			phone = reader.readLine();
			match_phone = check_phone.matcher(phone);
		}

		// Create a Customer Object
		String[] split_name = name.split(" ", 0);
		String first_name = split_name[0];
		String last_name = split_name[1];

		// Get a new CustomerID by querying the database table
		int custID = DBNinja.getNewCustomerID();
		Customer cust = new Customer(custID, first_name, last_name, phone);
		DBNinja.addCustomer(cust);
	}

	// Helper function that views an order in detail
	public static void viewOrderDetails(Order o) throws SQLException, IOException {
		// Get all discounts and pizzas for an order
		DBNinja.getPizzasForOrder(o);
		DBNinja.getOrderDiscounts(o);

		System.out.println(o.toString());
		if(o.getDiscountList().isEmpty()) {
			System.out.println("NO ORDER DISCOUNTS");
		}
		else {
			System.out.print("ORDER DISCOUNTS:");
			for (Discount d : o.getDiscountList()) {
				System.out.print(" " + d.getDiscountName());
			}
			System.out.println();
		}

		for(Pizza p : o.getPizzaList()) {
			System.out.println(p.toString());
			if(p.getDiscounts().isEmpty()) {
				System.out.println("NO PIZZA DISCOUNTS");
			}
			else {
				System.out.print("PIZZA DISCOUNTS:");
				for (Discount d : p.getDiscounts()) {
					System.out.print(" " + d.getDiscountName());
				}
				System.out.println();
			}
		}
	}

	// View any orders that are not marked as completed
	public static void ViewOrders() throws SQLException, IOException {
		/*
		 * This method allows the user to select between three different views of the
		 * Order history:
		 * The program must display:
		 * a. all open orders
		 * b. all completed orders
		 * c. all the orders (open and completed) since a specific date (inclusive)
		 * 
		 * After displaying the list of orders (in a condensed format) must allow the
		 * user to select a specific order for viewing its details.
		 * The details include the full order type information, the pizza information
		 * (including pizza discounts), and the order discounts.
		 * 
		 */

		ArrayList<Order> orderList;
		String option = "";
		String date = "";
		System.out.println("Would you like to:\n(a) display all orders [open or closed]\n(b) display all open orders\n" +
				"(c) display all completed [closed] orders\n(d) display orders since a specific date");
		option = reader.readLine();
		if(option.equals("a")) {
			orderList = DBNinja.getOrders(false);
		}
		else if(option.equals("b")) {
			orderList = DBNinja.getOrders(true);
		}
		else if(option.equals("c")) {
			orderList = DBNinja.getClosedOrders();
		}
		else if(option.equals("d")){
			System.out.println("What is the date you want to restrict by? (FORMAT= YYYY-MM-DD)");
			date = reader.readLine();
			Pattern check_date = Pattern.compile("^[0-9]{4}-[0-9]{2}-[0-9]{2}$", Pattern.CASE_INSENSITIVE);
			Matcher match_date = check_date.matcher(date);
			// Do not accept invalid phone numbers
			while (!match_date.find()) {
				System.out.println("What is the date you want to restrict by? (FORMAT= YYYY-MM-DD)");
				date = reader.readLine();
				match_date = check_date.matcher(date);
			}
			orderList = DBNinja.getOrdersByDate(date);
		}
		else {
			System.out.println("I don't understand that input, returning to menu");
			return;
		}

		if(orderList.isEmpty()) {
			System.out.println("No orders to display, returning to menu.");
			return;
		}
		else {
			for(Order order : orderList) {
				System.out.println(order.toSimplePrint());
			}
		}

		int ID = 0;
		do {
			System.out.println("Which order would you like to see in detail? Enter the number (-1 to exit): ");
			option = reader.readLine();
			try {
				ID = Integer.parseInt(option);
				// Check if the chosen order is in the returned list
				// Exit to menu if it was not found
				Order viewOrder = findOrderInList(orderList, ID);
				if(viewOrder == null && ID != -1) {
					System.out.println("Incorrect entry, returning to menu.");
					return;
				}
				else {
					viewOrderDetails(viewOrder);
				}
			}
			catch(Exception ignored) {
				System.out.println("Please enter a numeric value.");
			}
		} while(ID != -1);
	}

	// Helper function that gets an order with a provided orderID from a list
	// If no order is found, return null
	public static Order findOrderInList(ArrayList<Order> orderList, int orderID) throws SQLException, IOException {
		for(Order order : orderList) {
			if(order.getOrderID() == orderID) {
				return order;
			}
		}
		return null;
	}

	// When an order is completed, we need to make sure it is marked as complete
	public static void MarkOrderAsComplete() throws SQLException, IOException {
		/*
		 * All orders that are created through java (part 3, not the orders from part 2)
		 * should start as incomplete
		 * 
		 * When this method is called, you should print all of the "open" orders marked
		 * and allow the user to choose which of the incomplete orders they wish to mark
		 * as complete
		 * 
		 */

		// Get all open orders
		ArrayList<Order> openOrderList = DBNinja.getOrders(true);
		if(openOrderList == null) {
			System.out.println("There are no open orders currently... returning to menu...");
			return;
		}
		else {
			for(Order order : openOrderList) {
				System.out.println(order.toSimplePrint());
			}
		}

		int orderID = 0;
		String option = "";
		System.out.println("Which order would you like mark as complete? Enter the OrderID: ");
		option = reader.readLine();
		try {
			orderID = Integer.parseInt(option);
		}
		catch(Exception ignored) {
			System.out.println("Incorrect entry, not an option");
			return;
		}

		// Find an open order with the given orderID
		Order markComplete = findOrderInList(openOrderList, orderID);
		if(markComplete != null) {
			DBNinja.completeOrder(markComplete);
		}
		else {
			System.out.println("Incorrect entry, not an option");
		}
	}

	public static void ViewInventoryLevels() throws SQLException, IOException {
		/*
		 * Print the inventory. Display the topping ID, name, and current inventory
		 */
		DBNinja.printInventory();
	}

	public static void AddInventory() throws SQLException, IOException {
		/*
		 * This should print the current inventory and then ask the user which topping
		 * (by ID) they want to add more to and how much to add
		 */

		// User Input Prompts...
		ViewInventoryLevels();
		System.out.println("Which topping do you want to add inventory to? Enter the number: ");
		String toppingID = reader.readLine();
		// Check that the ID exists in the database
		Topping topping = DBNinja.findToppingByID(toppingID);
		while(topping == null) {
			System.out.println("Incorrect entry, not an option");
			System.out.println("Which topping do you want to add inventory to? Enter the number: ");
			toppingID = reader.readLine();
			topping = DBNinja.findToppingByID(toppingID);
		}

		System.out.println("How many units would you like to add? ");
		String option = reader.readLine();
		int unitsToAdd = 0;
		// Handle exceptions with parseInt
		try {
			unitsToAdd = Integer.parseInt(option);
		}
		catch(Exception ignored) {

		}
		while(unitsToAdd <= 0) {
			// Prompt the user again if the units to add is not greater than 0
			System.out.println("Incorrect entry, not an option");
			System.out.println("How many units would you like to add? ");
			option = reader.readLine();
			// Handle exceptions with parseInt
			try {
				unitsToAdd = Integer.parseInt(option);
			}
			catch(Exception ignored) {

			}
		}

		// Update the topping units in the database
		DBNinja.addToInventory(topping, unitsToAdd);
	}

	// A method that builds a pizza. Used in our add new order method
	public static Pizza buildPizza(int orderID) throws SQLException, IOException {
		/*
		 * This is a helper method for first menu option.
		 * 
		 * It should ask which size pizza the user wants and the crustType.
		 * 
		 * Once the pizza is created, it should be added to the DB.
		 * 
		 * We also need to add toppings to the pizza. (Which means we not only need to
		 * add toppings here, but also our bridge table)
		 * 
		 * We then need to add pizza discounts (again, to here and to the database)
		 * 
		 * Once the discounts are added, we can return the pizza
		 */
		String size = "";
		String crustType = "";

		Map<Topping, Boolean> toppingsUsed = new HashMap<Topping, Boolean>();
		ArrayList<Discount> discountsList = new ArrayList<Discount>();

		// User Input Prompts...
		System.out.println("What size is the pizza?");
		System.out.println("1." + DBNinja.size_s);
		System.out.println("2." + DBNinja.size_m);
		System.out.println("3." + DBNinja.size_l);
		System.out.println("4." + DBNinja.size_xl);

		// Picking A Pizza Size

		int choice = -1;
		while (choice < 1 || choice > 4) {
			try {
				System.out.println("Enter the corresponding number: ");
				String option = reader.readLine();
				choice = Integer.parseInt(option);
			} catch (Exception ignored) {
				System.out.println("Please enter a number between 1 and 4.");
			}
			switch (choice) {
				case 1:
					size = DBNinja.size_s;
					break;
				case 2:
					size = DBNinja.size_m;
					break;
				case 3:
					size = DBNinja.size_l;
					break;
				case 4:
					size = DBNinja.size_xl;
					break;
				default:
					System.out.println("Please enter a number between 1 and 4.");
					break;
			}
		}

		// Picking a Crust Type
		System.out.println("What crust for this pizza?");
		System.out.println("1." + DBNinja.crust_thin);
		System.out.println("2." + DBNinja.crust_orig);
		System.out.println("3." + DBNinja.crust_pan);
		System.out.println("4." + DBNinja.crust_gf);

		choice = -1;
		while (choice < 1 || choice > 4) {
			try {
				System.out.println("Enter the corresponding number: ");
				String option = reader.readLine();
				choice = Integer.parseInt(option);
			} catch (Exception e) {
				System.out.println("Please enter a number between 1 and 4.");
			}
			switch (choice) {
				case 1:
					crustType = "Thin";
					break;
				case 2:
					crustType = "Original";
					break;
				case 3:
					crustType = "Pan";
					break;
				case 4:
					crustType = "Gluten-Free";
					break;
				default:
					System.out.println("Please enter a number between 1 and 4.");
					break;
			}
		}

		// Printing Out Available Toppings
		DBNinja.printInventory();

		String inputtedTopID = "";

		while (!inputtedTopID.equals("-1")) {
			System.out.println("Which topping do you want to add? Enter the TopID. Enter -1 to stop adding toppings: ");
			inputtedTopID = reader.readLine();

			if(!inputtedTopID.equals("-1")) {
				Topping selectedTopping = DBNinja.findToppingByID(inputtedTopID);

				if (selectedTopping == null) {
					System.out.println("That topping was not found.");
					continue;
				}

				String extraRequested = "";
				while (!extraRequested.equals("y") && !extraRequested.equals("n")) {
					System.out.println("Do you want to add extra topping? Enter y/n");
					extraRequested = reader.readLine().toLowerCase();
				}

				boolean requestExtra;
				if (extraRequested.equals("y"))
					requestExtra = true;
				else
					requestExtra = false;

				if (DBNinja.checkIfEnoughTopping(Integer.parseInt(inputtedTopID), size, requestExtra))
					toppingsUsed.put(selectedTopping, requestExtra);
				else
					System.out.println("We don't have enough of that topping to add it...");
			}
		}

		////////////////////////////////////////
		/////////// ADDING FUNCTIONS ///////////
		////////////////////////////////////////
		double baseCost = DBNinja.getBaseBusPrice(size, crustType);
		double basePrice = DBNinja.getBaseCustPrice(size, crustType);

		// Get a timestamp for the pizza
		String timestamp = createTimestamp();

		// PizzaID uses 1 as a dummy value. We set the pizza's actual ID in the addPizza method in DBNinja.
		Pizza thePizza = new Pizza(1, size, crustType, orderID, "Incomplete", timestamp, basePrice,
				baseCost);

		for (Topping t : toppingsUsed.keySet()) {
			thePizza.addToppings(t, toppingsUsed.get(t));
		}

		// Ask if they want a discount
		String discountRequest = "";
		while (!discountRequest.equals("y") && !discountRequest.equals("n")) {
			System.out.println("Do you want to add discounts to this Pizza? Enter y/n?");
			discountRequest = reader.readLine().toLowerCase();
		}

		// Loop for asking which discounts they want
		if (discountRequest.equals("y")) {
			String discountChoice = "";
			while (!discountChoice.equals("-1")) {
				DBNinja.printDiscounts();
				System.out.println(
						"Which Pizza Discount do you want to add? Enter the DiscountID. Enter -1 to stop adding Discounts: ");
				discountChoice = reader.readLine();
				Discount selectedDiscount = DBNinja.findDiscountByID(discountChoice);
				if (selectedDiscount != null) {
					if(isDiscountInList(discountsList, discountChoice)) {
						System.out.println("This discount has already been applied.");
					}
					else if(!selectedDiscount.isPercent() && (thePizza.getCustPrice() - selectedDiscount.getAmount() < 0)) {
						System.out.println("Cannot add a discount that drops the price below 0.");
					}
					else {
						discountsList.add(selectedDiscount);
					}
				}
			}
		}

		// Add used discounts to the pizza
		for(Discount d : discountsList) {
			thePizza.addDiscounts(d);
		}

		return thePizza;
	}

	public static void PrintReports() throws SQLException, NumberFormatException, IOException {
		/*
		 * This method asks the use which report they want to see and calls the DBNinja
		 * method to print the appropriate report.
		 * 
		 */

		// User Input Prompts...

		System.out.println(
				"Which report do you wish to print? Enter\n(a) ToppingPopularity\n(b) ProfitByPizza\n(c) ProfitByOrderType:");
		String choice = reader.readLine();
		if (!choice.equals("a") && !choice.equals("b") && !choice.equals("c")) {
			System.out.println("I don't understand that input... returning to menu...");
			return;
		}
		if (choice.equals("a")) {
			DBNinja.printToppingPopReport();
		} else if (choice.equals("b")) {
			DBNinja.printProfitByPizzaReport();
		} else {
			DBNinja.printProfitByOrderType();
		}

	}

	// Prompt - NO CODE SHOULD TAKE PLACE BELOW THIS LINE
	// DO NOT EDIT ANYTHING BELOW HERE, THIS IS NEEDED TESTING.
	// IF YOU EDIT SOMETHING BELOW, IT BREAKS THE AUTOGRADER WHICH MEANS YOUR GRADE
	// WILL BE A 0 (zero)!!

	public static void PrintMenu() {
		System.out.println("\n\nPlease enter a menu option:");
		System.out.println("1. Enter a new order");
		System.out.println("2. View Customers ");
		System.out.println("3. Enter a new Customer ");
		System.out.println("4. View orders");
		System.out.println("5. Mark an order as completed");
		System.out.println("6. View Inventory Levels");
		System.out.println("7. Add Inventory");
		System.out.println("8. View Reports");
		System.out.println("9. Exit\n\n");
		System.out.println("Enter your option: ");
	}

	/*
	 * autograder controls....do not modiify!
	 */

	public final static String autograder_seed = "6f1b7ea9aac470402d48f7916ea6a010";

	private static void autograder_compilation_check() {

		try {
			Order o = null;
			Pizza p = null;
			Topping t = null;
			Discount d = null;
			Customer c = null;
			ArrayList<Order> alo = null;
			ArrayList<Discount> ald = null;
			ArrayList<Customer> alc = null;
			ArrayList<Topping> alt = null;
			double v = 0.0;
			String s = "";

			DBNinja.addOrder(o);
			DBNinja.addPizza(p);
			DBNinja.useTopping(p, t, false);
			DBNinja.usePizzaDiscount(p, d);
			DBNinja.useOrderDiscount(o, d);
			DBNinja.addCustomer(c);
			DBNinja.completeOrder(o);
			alo = getOrders(false);
			o = DBNinja.getLastOrder();
			alo = DBNinja.getOrdersByDate("01/01/1999");
			ald = DBNinja.getDiscountList();
			d = DBNinja.findDiscountByName("Discount");
			alc = DBNinja.getCustomerList();
			c = DBNinja.findCustomerByPhone("0000000000");
			alt = DBNinja.getToppingList();
			t = DBNinja.findToppingByName("Topping");
			DBNinja.addToInventory(t, 1000.0);
			v = DBNinja.getBaseCustPrice("size", "crust");
			v = DBNinja.getBaseBusPrice("size", "crust");
			DBNinja.printInventory();
			DBNinja.printToppingPopReport();
			DBNinja.printProfitByPizzaReport();
			DBNinja.printProfitByOrderType();
			s = DBNinja.getCustomerName(0);
		} catch (SQLException | IOException e) {
			e.printStackTrace();
		}
	}

}
