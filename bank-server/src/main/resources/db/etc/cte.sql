SELECT a.* FROM
(select *, ROW_NUMBER() over (PARTITION BY city order by id) n from account
WHERE city IN ('dortmund','liverpool','dusseldorf','winnipeg','pretoria','barcelona','bend','vienna','melbourne','chicago','lyon','glasgow','lisbon','durban','hong kong','essen','athens','las vegas','edmonton','charlotte','sydney','johannesburg','zagraeb','seville','madrid','kimberley','atlanta','zaragoza','eugene','stockholm','boston','zurich','dublin','rotterdam','nashville','st louis','indianapolis','palermo','milan','oslo','birmingham','detroit','berlin','minneapolis','hamburg','buenos aires','san diego','riga','cape town','lodz','frankfurt','turin','washington dc','singapore','krakov','shanghai','antwerp','hague','regina','dallas','london','salem','miami','brandon','bucharest','cologne','portland','tacoma','seattle','warsaw','los angeles','naples','jakarta','rome','dryden','bratislava','copenhagen','toulouse','helsinki','amsterdam','prague','sofia','munich','salt lake city','brussels','grahamstown','ghent','budapest','leeds','salvador','beijing','leipzig','paris','stuttgart','houston','calgary','san francisco','tallinn','valencia','rio de janeiro','new york','manchester','tokyo','phoenix','sintra','marseille','belfast','sao paulo')
) a WHERE n <= 10 order by a.id;

-- CTE starts
explain
WITH accounts AS (
    SELECT
        *,
        ROW_NUMBER() OVER (PARTITION BY city ORDER BY id) n
    FROM account
    WHERE city IN ('dortmund','liverpool','dusseldorf','winnipeg','pretoria','barcelona','bend','vienna','melbourne','chicago','lyon','glasgow','lisbon','durban','hong kong','essen','athens','las vegas','edmonton','charlotte','sydney','johannesburg','zagraeb','seville','madrid','kimberley','atlanta','zaragoza','eugene','stockholm','boston','zurich','dublin','rotterdam','nashville','st louis','indianapolis','palermo','milan','oslo','birmingham','detroit','berlin','minneapolis','hamburg','buenos aires','san diego','riga','cape town','lodz','frankfurt','turin','washington dc','singapore','krakov','shanghai','antwerp','hague','regina','dallas','london','salem','miami','brandon','bucharest','cologne','portland','tacoma','seattle','warsaw','los angeles','naples','jakarta','rome','dryden','bratislava','copenhagen','toulouse','helsinki','amsterdam','prague','sofia','munich','salt lake city','brussels','grahamstown','ghent','budapest','leeds','salvador','beijing','leipzig','paris','stuttgart','houston','calgary','san francisco','tallinn','valencia','rio de janeiro','new york','manchester','tokyo','phoenix','sintra','marseille','belfast','sao paulo')
)
--CTE ends
SELECT *
FROM accounts
WHERE n <= 5
ORDER BY city;