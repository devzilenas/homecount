UPDATE income_expense
	SET ondate = CAST(CONCAT_WS(
                                             '-',
                                            (SELECT "year"    FROM years    ORDER BY RAND() LIMIT 1),
                                            (SELECT month FROM months ORDER BY RAND() LIMIT 1),
                                            (SELECT day      FROM days    WHERE day <= 28  ORDER BY RAND() LIMIT 1)
                           ) AS DATE);
