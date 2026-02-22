package com.app.history.reader;

import com.app.utils.Result;
import com.app.utils.StockError;

public interface MovementReader {
    Result<MovementStream, StockError> readMovements();
}
