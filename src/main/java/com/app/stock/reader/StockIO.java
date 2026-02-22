package com.app.stock.reader;

import com.app.stock.StockData;
import com.app.utils.Result;
import com.app.utils.StockError;

public interface StockIO {
    Result<StockData, StockError> readStocks();

    Result<Void, StockError> writeStocks(StockData stockData, String s);
}
