package com.app.history.reader;

import com.app.history.model.MovementRecord;
import com.app.history.model.MovementRecordFactory;
import com.app.utils.Result;
import com.app.utils.StockError;

import java.util.stream.Stream;

public class MovementStream implements AutoCloseable {
    private final Stream<String> source;
    private final Stream<Result<MovementRecord, StockError>> stream;

    public MovementStream(Stream<String> source) {
        this.source = source;
        this.stream = source.map(MovementRecordFactory::createFromCsv);
    }

    public Stream<Result<MovementRecord, StockError>> stream() {
        return stream;
    }

    @Override
    public void close() {
        source.close();
    }
}
