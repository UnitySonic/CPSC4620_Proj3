/* Authors: Tim Koehler and Michael Merritt */

/* Create the Pizzeria schema */
DROP SCHEMA IF EXISTS Pizzeria;
CREATE SCHEMA Pizzeria;
use Pizzeria;

/* Create database tables */
CREATE TABLE discount(
	DiscountID INT AUTO_INCREMENT PRIMARY KEY,
	DiscountName VARCHAR(50) NOT NULL,
    DiscountPercentOff NUMERIC(4,2),
    DiscountDollarsOff NUMERIC(4,2)
);
ALTER TABLE discount AUTO_INCREMENT=1;

CREATE TABLE topping(
	ToppingID INT AUTO_INCREMENT PRIMARY KEY,
    ToppingName VARCHAR(20) NOT NULL,
    ToppingPricePerUnit NUMERIC(3,2) NOT NULL,
    ToppingCostPerUnit NUMERIC(3,2) NOT NULL,
    ToppingCurrentInventory NUMERIC(4,0) NOT NULL,
    ToppingMinimumInventory NUMERIC(4,0) NOT NULL,
    ToppingSmallSizeUnits NUMERIC(4,2) NOT NULL,
    ToppingMediumSizeUnits NUMERIC(4,2) NOT NULL,
    ToppingLargeSizeUnits NUMERIC(4,2) NOT NULL,
    ToppingXLargeSizeUnits NUMERIC(4,2) NOT NULL,
    CONSTRAINT CHK_CURRENT_INV CHECK (ToppingCurrentInventory >= ToppingMinimumInventory)
);
ALTER TABLE topping AUTO_INCREMENT=100;

CREATE TABLE customer(
	CustomerID INT AUTO_INCREMENT PRIMARY KEY,
    CustomerFName VARCHAR(50) NOT NULL,
    CustomerLName VARCHAR(50) NOT NULL,
    CustomerPhoneNumber VARCHAR(10) NOT NULL,
    CustomerStreetAddress VARCHAR(75),
    CustomerCity VARCHAR(50),
    CustomerState VARCHAR(2),
    CustomerZipcode VARCHAR(5)
);
ALTER TABLE customer AUTO_INCREMENT=1;

CREATE TABLE customer_order(
	Customer_OrderID INT AUTO_INCREMENT PRIMARY KEY,
    Customer_OrderCustomerPrice NUMERIC(5,2) NOT NULL,
    Customer_OrderBusinessCost NUMERIC(5,2) NOT NULL,
    Customer_OrderType VARCHAR(20) NOT NULL,
    Customer_OrderStatus VARCHAR(20) NOT NULL,
    Customer_OrderTimestamp DATETIME NOT NULL
);
ALTER TABLE customer_order AUTO_INCREMENT=100;

CREATE TABLE base_price_and_cost(
	Base_Price_And_CostPizzaSize VARCHAR(15) NOT NULL,
    Base_Price_And_CostCrustType VARCHAR(15) NOT NULL,
    Base_Price_And_CostBasePrice NUMERIC(5,2) NOT NULL,
    Base_Price_And_CostBaseCost NUMERIC(5,2) NOT NULL,
    PRIMARY KEY(Base_Price_And_CostPizzaSize, Base_Price_And_CostCrustType),
    CONSTRAINT BASE_PRICE_AND_COST_UI1 UNIQUE(Base_Price_And_CostPizzaSize, Base_Price_And_CostCrustType)
);

CREATE TABLE pizza(
	PizzaID INT AUTO_INCREMENT PRIMARY KEY,
    PizzaOrderID INT NOT NULL,
    PizzaSize VARCHAR(15) NOT NULL,
    PizzaCrustType VARCHAR(15) NOT NULL,
    PizzaPrice NUMERIC(5,2) NOT NULL,
    PizzaCost NUMERIC(5,2) NOT NULL,
    PizzaCurrentState VARCHAR(15) NOT NULL,
    FOREIGN KEY(PizzaOrderID) REFERENCES customer_order(Customer_OrderID)
    ON DELETE CASCADE,
    FOREIGN KEY(PizzaSize, PizzaCrustType) REFERENCES base_price_and_cost(Base_Price_And_CostPizzaSize, Base_Price_And_CostCrustType)
    ON DELETE CASCADE
);
ALTER TABLE pizza AUTO_INCREMENT=1;

CREATE TABLE pizza_discount(
	Pizza_DiscountDiscount_ID INT NOT NULL,
    Pizza_DiscountPizza_ID INT NOT NULL,
    PRIMARY KEY(Pizza_DiscountDiscount_ID, Pizza_DiscountPizza_ID),
    CONSTRAINT PIZZA_DISCOUNT_UI1 UNIQUE(Pizza_DiscountDiscount_ID, Pizza_DiscountPizza_ID),
    FOREIGN KEY(Pizza_DiscountDiscount_ID) REFERENCES discount(DiscountID)
    ON DELETE CASCADE,
    FOREIGN KEY(Pizza_DiscountPizza_ID) REFERENCES pizza(PizzaID)
    ON DELETE CASCADE
);

CREATE TABLE pizza_topping(
	Pizza_ToppingPizzaID INT NOT NULL,
    Pizza_ToppingToppingID INT NOT NULL,
    Pizza_ToppingExtraRequested BOOLEAN NOT NULL DEFAULT FALSE,
    PRIMARY KEY(Pizza_ToppingPizzaID, Pizza_ToppingToppingID),
    CONSTRAINT PIZZA_TOPPING_UI1 UNIQUE(Pizza_ToppingPizzaID, Pizza_ToppingToppingID),
    FOREIGN KEY(Pizza_ToppingPizzaID) REFERENCES pizza(PizzaID)
    ON DELETE CASCADE,
    FOREIGN KEY(Pizza_ToppingToppingID) REFERENCES topping(ToppingID)
	ON DELETE CASCADE
);

CREATE TABLE order_discount(
	Order_DiscountDiscountID INT NOT NULL,
    Order_DiscountOrderID INT NOT NULL,
    PRIMARY KEY(Order_DiscountDiscountID, Order_DiscountOrderID),
    CONSTRAINT ORDER_DISCOUNT_UI1 UNIQUE(Order_DiscountDiscountID, Order_DiscountOrderID),
    FOREIGN KEY (Order_DiscountDiscountID) REFERENCES discount(DiscountID)
    ON DELETE CASCADE,
    FOREIGN KEY (Order_DiscountOrderID) REFERENCES customer_order(Customer_OrderID)
    ON DELETE CASCADE
);

CREATE TABLE dine_in(
	Dine_InOrderID INT PRIMARY KEY,
    Dine_InTable_Number NUMERIC(3,0) NOT NULL,
    FOREIGN KEY(Dine_InOrderID) REFERENCES customer_order(Customer_OrderID)
    ON DELETE CASCADE
);

CREATE TABLE pickup(
	PickupOrder_ID INT PRIMARY KEY,
    PickupCustomer_ID INT NOT NULL,
    FOREIGN KEY(PickupOrder_ID) REFERENCES customer_order(Customer_OrderID)
    ON DELETE CASCADE,
    FOREIGN KEY(PickupCustomer_ID) REFERENCES customer(CustomerID)
    ON DELETE CASCADE
);

CREATE TABLE delivery(
	DeliveryOrderID INT PRIMARY KEY,
    DeliveryCustomerID INT NOT NULL,
    FOREIGN KEY(DeliveryOrderID) REFERENCES customer_order(Customer_OrderID)
    ON DELETE CASCADE,
    FOREIGN KEY(DeliveryCustomerID) REFERENCES customer(CustomerID)
    ON DELETE CASCADE
);