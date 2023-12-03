/* Authors: Tim Koehler and Michael Merritt */

use Pizzeria;

DROP VIEW IF EXISTS ToppingPopularity;
DROP VIEW IF EXISTS ProfitByPizza;
DROP VIEW IF EXISTS ProfitByOrderType;

/* Create a view for ToppingPopularity */
CREATE VIEW ToppingPopularity AS
	SELECT 
		t.ToppingName AS Topping, 
        SUM(CASE 
            WHEN pt.Pizza_ToppingExtraRequested = true THEN 2 
            WHEN pt.Pizza_ToppingExtraRequested = false THEN 1
            ELSE 0  
        END) AS ToppingCount
	FROM 
		topping t
	LEFT JOIN 
		pizza_topping pt ON t.ToppingID = pt.Pizza_ToppingToppingID 
	GROUP BY 
		t.ToppingName
	ORDER BY ToppingCount DESC;

/* Create a view for ProfitByPizza */
CREATE VIEW ProfitByPizza AS
	SELECT
		p.PizzaSize AS Size,
		p.PizzaCrustType AS Crust,
		SUM(p.PizzaPrice - p.PizzaCost) AS Profit,
		DATE_FORMAT(o.Customer_OrderTimeStamp, '%c/%Y') AS OrderMonth
	FROM
		pizza p
	JOIN
		customer_order o ON p.PizzaOrderID = o.Customer_OrderID
	GROUP BY
		Size, Crust, OrderMonth
	ORDER BY
		Profit DESC;

/* Create a view for ProfitByOrderType */
CREATE VIEW ProfitByOrderType AS
	SELECT
		o.Customer_OrderType AS customerType,
        DATE_FORMAT(o.Customer_OrderTimeStamp, '%c/%Y') AS OrderMonth,
        SUM(o.Customer_OrderCustomerPrice) AS TotalOrderPrice,
        SUM(o.Customer_OrderBusinessCost) AS TotalOrderCost,
        SUM(o.Customer_OrderCustomerPrice - o.Customer_OrderBusinessCost) AS Profit
	FROM
		customer_order o
	GROUP BY
		customerType, OrderMonth
	UNION
    SELECT
        '' AS customerType,
        'Grand Total' AS OrderMonth,
        SUM(o.Customer_OrderCustomerPrice) AS TotalOrderPrice,
        SUM(o.Customer_OrderBusinessCost) AS TotalOrderCost,
        SUM(o.Customer_OrderCustomerPrice - o.Customer_OrderBusinessCost) AS Profit
    FROM customer_order o;


/* Show contents of all views */
SELECT * FROM ToppingPopularity;
SELECT * FROM ProfitByPizza;
SELECT * FROM ProfitByOrderType;