/* Authors: Tim Koehler and Michael Merritt */

use Pizzeria;
    
    /* populate topping */
    INSERT INTO topping(ToppingName, ToppingPricePerUnit, ToppingCostPerUnit, ToppingCurrentInventory, ToppingMinimumInventory, 
    ToppingSmallSizeUnits, ToppingMediumSizeUnits, ToppingLargeSizeUnits, ToppingXLargeSizeUnits)
    VALUES
		("Pepperoni", 1.25, 0.2, 100, 50, 2, 2.75, 3.5, 4.5),
        ("Sausage", 1.25, 0.15, 100, 50, 2.5, 3, 3.5, 4.25),
        ("Ham", 1.5, 0.15, 78, 25, 2, 2.5, 3.25, 4),
        ("Chicken", 1.75, 0.25, 56, 25, 1.5, 2, 2.25, 3),
        ("Green Pepper", 0.5, 0.02, 79, 25, 1, 1.5, 2, 2.5),
        ("Onion", 0.5, 0.02, 85, 25, 1, 1.5, 2, 2.75),
        ("Roma Tomato", 0.75, 0.03, 86, 10, 2, 3, 3.5, 4.5),
        ("Mushrooms", 0.75, 0.1, 52, 50, 1.5, 2, 2.5, 3),
        ("Black Olives", 0.6, 0.1, 39, 25, 0.75, 1, 1.5, 2),
        ("Pineapple", 1, 0.25, 15, 0, 1, 1.25, 1.75, 2),
        ("Jalapenos", 0.5, 0.05, 64, 0, 0.5, 0.75, 1.25, 1.75),
        ("Banana Peppers", 0.5, 0.05, 36, 0, 0.6, 1, 1.3, 1.75),
        ("Regular Cheese", 0.5, 0.12, 250, 50, 2, 3.5, 5, 7),
        ("Four Cheese Blend", 1, 0.15, 150, 25, 2, 3.5, 5, 7),
        ("Feta Cheese", 1.5, 0.18, 75, 0, 1.75, 3, 4, 5.5),
        ("Goat Cheese", 1.5, 0.2, 54, 0, 1.6, 2.75, 4, 5.5),
        ("Bacon", 1.5, 0.25, 89, 0, 1, 1.5, 2, 3);
        
    /* populate discount */
	INSERT INTO discount(DiscountName, DiscountPercentOff, DiscountDollarsOff)
    VALUES
		("Employee", 15.00, NULL),
		("Lunch Special Medium", NULL, 1.00),
		("Lunch Special Large", NULL, 2.00),
		("Specialty Pizza", NULL, 1.50),
		("Happy Hour", 10.00, NULL),
		("Gameday Special", 20.00, NULL);

    /* populate price and costs */
    INSERT INTO base_price_and_cost
    VALUES
        ("Small", "Thin", 3, 0.5),
        ("Small", "Original", 3, 0.75),
        ("Small", "Pan", 3.5, 1),
        ("Small", "Gluten-Free", 4, 2),
        ("Medium", "Thin", 5, 1),
        ("Medium", "Original", 5, 1.5),
        ("Medium", "Pan", 6, 2.25),
        ("Medium", "Gluten-Free", 6.25, 3),
        ("Large", "Thin", 8, 1.25),
        ("Large", "Original", 8, 2),
        ("Large", "Pan", 9, 3),
        ("Large", "Gluten-Free", 9.5, 4),
        ("XLarge", "Thin", 10, 2),
        ("XLarge", "Original", 10, 3),
        ("XLarge", "Pan", 11.5, 4.5),
        ("XLarge", "Gluten-Free", 12.5, 6);

/***************************/
	/* ORDER ENTERING */
/***************************/
/***************************/
    /* Order 1 */
/***************************/
    INSERT INTO customer_order(Customer_OrderCustomerPrice, Customer_OrderBusinessCost, Customer_OrderType, Customer_OrderStatus, 
    Customer_OrderTimestamp)
    VALUES(20.75, 3.68, "dinein", "Completed", "2023-03-05 12:03:00");	
    
    INSERT INTO dine_in
    VALUES(100, 21);
    
    INSERT INTO pizza(PizzaOrderID, PizzaSize, PizzaCrustType, PizzaPrice, PizzaCost, PizzaCurrentState)
    VALUES(100, "Large", "Thin", 20.75, 3.68, "Completed");
    
    INSERT INTO pizza_topping
    VALUES
		(1, 112, TRUE),
        (1, 100, FALSE),
        (1, 101, FALSE);
	
    INSERT INTO pizza_discount
	VALUES
		(3, 1);

/***********************/
	   /* Order 2 */
/*************************/
    INSERT INTO customer_order(Customer_OrderCustomerPrice, Customer_OrderBusinessCost, Customer_OrderType, Customer_OrderStatus, 
    Customer_OrderTimestamp)
    VALUES(19.78, 4.63, "dinein", "Completed", "2023-04-03 12:05:00");	
    
    INSERT INTO dine_in
    VALUES(101, 4);
    
    INSERT INTO pizza(PizzaOrderID, PizzaSize, PizzaCrustType, PizzaPrice, PizzaCost, PizzaCurrentState)
    VALUES
		(101, "Medium", "Pan", 12.85, 3.23, "Completed"),
        (101, "Small", "Original", 6.93, 1.40, "Completed");
    
    INSERT INTO pizza_topping
    VALUES
		(2, 114, FALSE),
        (2, 108, FALSE),
        (2, 106, FALSE),
        (2, 107, FALSE),
        (2, 111, FALSE),
        (3, 112, FALSE),
        (3, 103, FALSE),
        (3, 111, FALSE);
	
    INSERT INTO pizza_discount
	VALUES
		(2, 2),
        (4, 2);
        
/*******************/
    /* Order 3 */
/*******************/
    INSERT INTO customer_order(Customer_OrderCustomerPrice, Customer_OrderBusinessCost, Customer_OrderType, Customer_OrderStatus, 
    Customer_OrderTimestamp)
    VALUES(89.28, 19.80, "pickup", "Completed", "2023-03-03 21:30:00");	
    
    INSERT INTO customer(CustomerFName, CustomerLName, CustomerPhoneNumber, CustomerStreetAddress, CustomerCity, CustomerState, 
    CustomerZipcode)
    VALUES
		("Andrew", "Wilkes-Krier", "8642545861", NULL, NULL, NULL, NULL);
    
    INSERT INTO pickup
    VALUES(102, 1);
    
    INSERT INTO pizza(PizzaOrderID, PizzaSize, PizzaCrustType, PizzaPrice, PizzaCost, PizzaCurrentState)
    VALUES
		(102, "Large", "Original", 14.88, 3.30, "Completed"),
        (102, "Large", "Original", 14.88, 3.30, "Completed"),
        (102, "Large", "Original", 14.88, 3.30, "Completed"),
        (102, "Large", "Original", 14.88, 3.30, "Completed"),
        (102, "Large", "Original", 14.88, 3.30, "Completed"),
        (102, "Large", "Original", 14.88, 3.30, "Completed");
    
    INSERT INTO pizza_topping
    VALUES
		(4, 112, FALSE),
        (4, 100, FALSE),
        (5, 112, FALSE),
        (5, 100, FALSE),
        (6, 112, FALSE),
        (6, 100, FALSE),
        (7, 112, FALSE),
        (7, 100, FALSE),
        (8, 112, FALSE),
        (8, 100, FALSE),
        (9, 112, FALSE),
        (9, 100, FALSE);

/*******************/	
    /* Order 4 */
/*******************/
    INSERT INTO customer_order(Customer_OrderCustomerPrice, Customer_OrderBusinessCost, Customer_OrderType, Customer_OrderStatus, 
    Customer_OrderTimestamp)
    VALUES(86.19, 23.62, "delivery", "Completed", "2023-04-20 19:11:00");	
    
    INSERT INTO delivery
    VALUES(103, 1);
    
    INSERT INTO pizza(PizzaOrderID, PizzaSize, PizzaCrustType, PizzaPrice, PizzaCost, PizzaCurrentState)
    VALUES
		(103, "XLarge", "Original", 27.94, 9.19, "Completed"),
        (103, "XLarge", "Original", 31.50, 6.25, "Completed"),
        (103, "XLarge", "Original", 26.75, 8.18, "Completed");
    
    INSERT INTO pizza_topping
    VALUES
		(10, 113, FALSE),
        (10, 100, FALSE),
        (10, 101, FALSE),
        (11, 113, FALSE),
        (11, 102, TRUE),
        (11, 109, TRUE),
        (12, 113, FALSE),
        (12, 103, FALSE),
        (12, 116, FALSE);
    
    INSERT INTO pizza_discount
	VALUES
        (4, 11);
	
    INSERT INTO order_discount
    VALUES
		(6, 103);
        
	UPDATE customer
    SET 
		CustomerStreetAddress = "115 Party Blvd",
        CustomerCity = "Anderson",
        CustomerState = "SC",
        CustomerZipcode = "29621"	
	WHERE CustomerID = 1;

/*********************/
    /* Order 5 */
/*********************/
    INSERT INTO customer_order(Customer_OrderCustomerPrice, Customer_OrderBusinessCost, Customer_OrderType, Customer_OrderStatus, 
    Customer_OrderTimestamp)
    VALUES(27.45, 7.88, "pickup", "Completed", "2023-03-02 17:30:00");	
    
    INSERT INTO customer(CustomerFName, CustomerLName, CustomerPhoneNumber, CustomerStreetAddress, CustomerCity, CustomerState, 
    CustomerZipcode)
    VALUES
		("Matt", "Engers", "8644749953", NULL, NULL, NULL, NULL);
    
    INSERT INTO pickup
    VALUES(104, 2);
    
    INSERT INTO pizza(PizzaOrderID, PizzaSize, PizzaCrustType, PizzaPrice, PizzaCost, PizzaCurrentState)
    VALUES
		(104, "XLarge", "Gluten-Free", 27.45, 7.88, "Completed");

    INSERT INTO pizza_topping
    VALUES
		(13, 104, FALSE),
        (13, 105, FALSE),
        (13, 106, FALSE),
        (13, 107, FALSE),
        (13, 108, FALSE),
        (13, 115, FALSE);

    INSERT INTO pizza_discount
	VALUES
        (4, 13);

/********************/
    /* Order 6 */
/********************/
    INSERT INTO customer_order(Customer_OrderCustomerPrice, Customer_OrderBusinessCost, Customer_OrderType, Customer_OrderStatus, 
    Customer_OrderTimestamp)
    VALUES(25.81, 4.24, "delivery", "Completed", "2023-03-02 18:17:00");	
    
    INSERT INTO customer(CustomerFName, CustomerLName, CustomerPhoneNumber, CustomerStreetAddress, CustomerCity, CustomerState, 
    CustomerZipcode)
    VALUES
		("Frank", "Turner", "8642328944", "6745 Wessex St", "Anderson", "SC", "29621");
    
    INSERT INTO delivery
    VALUES(105, 3);
    
    INSERT INTO pizza(PizzaOrderID, PizzaSize, PizzaCrustType, PizzaPrice, PizzaCost, PizzaCurrentState)
    VALUES
		(105, "Large", "Thin", 25.81, 4.24, "Completed");

    INSERT INTO pizza_topping
    VALUES
		(14, 103, FALSE),
        (14, 104, FALSE),
        (14, 105, FALSE),
        (14, 107, FALSE),
        (14, 113, TRUE);

/********************/
    /* Order 7 */
/********************/
    INSERT INTO customer_order(Customer_OrderCustomerPrice, Customer_OrderBusinessCost, Customer_OrderType, Customer_OrderStatus, 
    Customer_OrderTimestamp)
    VALUES(37.25, 6, "delivery", "Completed", "2023-04-13 20:32:00");	
    
    INSERT INTO customer(CustomerFName, CustomerLName, CustomerPhoneNumber, CustomerStreetAddress, CustomerCity, CustomerState, 
    CustomerZipcode)
    VALUES
		("Milo", "Auckerman", "8648785679", "8879 Suburban Home", "Anderson", "SC", "29621");
    
    INSERT INTO delivery
    VALUES(106, 4);
    
    INSERT INTO pizza(PizzaOrderID, PizzaSize, PizzaCrustType, PizzaPrice, PizzaCost, PizzaCurrentState)
    VALUES
		(105, "Large", "Thin", 18.00, 2.75, "Completed"),
        (105, "Large", "Thin", 19.25, 3.25, "Completed");

    INSERT INTO pizza_topping
    VALUES
		(15, 113, TRUE),
        (16, 100, TRUE),
        (16, 112, FALSE);
	
    INSERT INTO order_discount
    VALUES
		(1, 106);