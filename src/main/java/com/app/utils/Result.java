package com.app.utils;

import java.util.*;
import java.util.function.*;
import java.util.stream.Stream;

/**
 * The Result class represents the result of a computation that can either be a success or a failure.
 * It provides methods for working with successful and failed results, transforming results, and handling errors.
 *
 * @param <R> The type of the successful result
 * @param <E> The type of the error message
 */
public interface Result<R, E> {

    /**
     * Converts an Optional into a Result. Returns a Failure with a default error message if the Optional is empty
     *
     * @param opt the Optional to be converted into a Result
     * @return a Result with the value of the Optional if present, otherwise a Failure with a default error message.
     */
    static <I> Result<I, String> fromOptional(Optional<I> opt) {
        return fromOptional(opt, "Empty");
    }

    /**
     * Converts an Optional into a Result. Returns a Failure with a specified error message if the Optional is empty
     *
     * @param opt   the Optional to be converted into a Result
     * @param error the specified error message
     * @return a Result with the value of the Optional if present, otherwise a Failure with the specified error message.
     */
    static <I> Result<I, String> fromOptional(Optional<I> opt, String error) {
        if (opt == null) {
            return failure("Optional is null");
        }
        return opt.<Result<I, String>>map(Result::success).orElseGet(() -> failure(error));
    }


    /**
     * This function sequences Results elements (Result<A, B>) inside a List and returns Result<List<A>, List<B>>.
     * If there are any errors in the individual results,
     * it collects them into a list of errors and returns Failure with List<B> as its error type.
     * If there are no errors in the individual results,
     * it collects the successful values into a list of values and returns Success with List<B> as its success type.
     *
     * @param list the List of Result<A, B> to be sequenced
     * @return Result<List<A>, List<B>> - a Success with list of all successful values (in order they appear in input list)
     * if all results were successful, otherwise a Failure with list of all errors (in order they appear in input list)
     */
    static <A, B> Result<List<A>, List<B>> sequence(List<Result<A, B>> list) {
        final ArrayList<A> success = new ArrayList<>();
        final ArrayList<B> error = new ArrayList<>();
        for (Result<A, B> element : list) {
            if (element.isSuccessful()) {
                success.add(element.getOrThrow());
            } else {
                error.add(element.error());
            }
        }
        if (error.isEmpty()) {
            return success(success);
        } else {
            return failure(error);
        }

    }

    /**
     * A function that sequences Results elements inside a List and returns Result<List<A>, B>.
     * If there are any errors in the individual results, it collects them into a list of errors and returns Failure with B as its error type.
     * If there are no errors in the individual results, it collects the successful values into a list of values and returns Success with B as its success type.
     *
     * @param list        the List of Result<A, B> to be sequenced
     * @param errorMapper the BinaryOperator used to map errors
     * @return a Result<List<A>, B> - a Success with a list of all successful values (in the order they appear in the input list)
     * if all results were successful, otherwise a Failure with the mapped error
     */
    static <A, B> Result<List<A>, B> sequenceWithErrorMapper(List<Result<A, B>> list, BinaryOperator<B> errorMapper) {
        final Result<List<A>, List<B>> sequenceResult = sequence(list);
        return sequenceResult.mapError(error -> error.stream().reduce(errorMapper).orElseThrow(() -> new RuntimeException("Error mapper should not be null")));
    }


    static <A, B> Result<List<A>, B> sequence(List<Result<A, B>> list, B error) {
        return sequenceWithErrorMapper(list, (a, b) -> error);
    }

    /**
     * A function that attempts to produce a value using the provided Supplier and returns a Result.
     *
     * @param producer     the Supplier used to produce a value
     * @param errorMessage the error message to be used in case of failure
     * @return a Result containing the produced value or the error message
     */
    static <A, B> Result<A, B> failable(Supplier<A> producer, B errorMessage) {
        try {
            return ofNullable(producer.get(), errorMessage);
        } catch (Exception _) {
            return Result.failure(errorMessage);
        }
    }

    /**
     * Creates a failure Result with a given error message.
     *
     * @param message an error message for the created failure Result.
     * @return a failure Result with the given error message.
     */
    static <V, U> Result<V, U> failure(U message) {
        return new Failure<>(message);
    }

    /**
     * Creates a success Result with a given value.
     *
     * @param value the value for the created success Result.
     * @return a success Result with the given value.
     */
    static <V, G> Result<V, G> success(V value) {
        return new Success<>(value);
    }

    /**
     * Creates a Result based on a given value: if the value is not null,
     * it creates a success Result with that value;
     * if the value is null, it creates a failure Result with a default error message.
     *
     * @param value the given value upon which the Result is based.
     * @return a Result based on the given value.
     */
    static <V> Result<V, String> ofNullable(V value) {
        return ofNullable(value, "Object is Null");
    }

    /**
     * Creates a Result based on a given value: if the value is not null,
     * it creates a success Result with that value; if the value is null,
     * it creates a failure Result with the provided error message.
     *
     * @param value the given value upon which the Result is based.
     * @param error an error message for the failure Result created when the value is null.
     * @return a Result based on the given value.
     */
    static <V, G> Result<V, G> ofNullable(V value, G error) {
        if (value == null) {
            return new Failure<>(error);
        }

        return new Success<>(value);
    }

    /**
     * Returns the value from this 'Result' if it is successful; otherwise returns 'defaultValue'.
     *
     * @param defaultValue the value to be returned if this 'Result' is a failure.
     *                     It can be any value that matches the return type.
     * @return the success value if this 'Result' is successful; 'defaultValue' otherwise.
     */
    default R getOrElse(final R defaultValue) {
        if (this.isSuccessful()) {
            return this.getOrThrow();
        } else {
            return defaultValue;
        }
    }

    /**
     * Works with two Results ('this' and 'other'). If 'this' Result is successful, it is returned.
     * Otherwise, the 'other' Result is returned.
     *
     * @param other an alternative Result to be returned when 'this' Result is a failure.
     *              It should not be null.
     * @return 'this' Result if it's successful; otherwise, 'other' Result.
     * @throws NullPointerException if 'other' is null.
     */
    default Result<R, E> or(Result<R, E> other) {
        Objects.requireNonNull(other, "Other should not be null");
        if (this.isSuccessful()) {
            return this;
        } else {
            return other;
        }
    }

    /**
     * Works with two Results ('this' and 'other'). If 'this' Result is successful, it is returned.
     * Otherwise, the Result supplied by 'other' is returned.
     *
     * @param other the supplier providing an alternative Result to be used when 'this' Result indicates failure.
     *              It should not be null.
     * @return 'this' Result if it's successful, 'other' Result otherwise.
     * @throws NullPointerException if 'other' is null.
     */
    default Result<R, E> or(Supplier<Result<R, E>> other) {
        Objects.requireNonNull(other, "Other should not be null");
        if (this.isSuccessful()) {
            return this;
        } else {
            return other.get();
        }
    }

    default R getOrElseGet(final Supplier<R> defaultValue) {
        if (this.isSuccessful()) {
            return this.getOrThrow();
        } else {
            return defaultValue.get();
        }
    }

    /**
     * If the result is successful, it returns the success value; otherwise it throws the RuntimeException supplied by 'e'.
     *
     * @param e a Supplier instance that supplies the RuntimeException to be thrown when the result is a failure.
     * @return the successful value.
     * @throws RuntimeException if the result is a failure.
     */
    R getOrThrow(Supplier<RuntimeException> e);

    /**
     * Returns the successful value if the result is a success. Throws an IllegalStateException otherwise.
     *
     * @return the successful value.
     * @throws IllegalStateException if the result is a failure.
     */
    default R getOrThrow() {
        return getOrThrow(() -> new IllegalStateException("Not successful"));
    }

    /**
     * Transforms the successful value of this result using the provided function 'f',
     * or leaves the result unchanged if it is a failure.
     *
     * @param f the transformation function that should not be null.
     *          This function is applied if this result is a success.
     * @return a new Result instance transformed by function 'f' if this result is a success,
     * the same instance (without any transformation) otherwise.
     * @throws NullPointerException if 'f' is null.
     */
    default <U> Result<U, E> map(Function<R, U> f) {
        Objects.requireNonNull(f, "Mapper should not be null");
        return this.flatMap(x -> success(f.apply(x)));
    }

    /**
     * Transforms the failure value of this result using the provided function 'f',
     * or leaves the result unchanged if it is a success.
     *
     * @param f the transformation function that should not be null.
     *          This function is applied if this result is a failure.
     * @return a new Result instance transformed by function 'f' if this result is a failure,
     * the same instance (without any transformation) if it is a success.
     * @throws NullPointerException if 'f' is null.
     */
    default <U> Result<R, U> mapError(Function<E, U> f) {
        Objects.requireNonNull(f, "Mapper should not be null");
        if (isSuccessful()) {
            return success(this.getOrThrow());
        } else {
            return failure(f.apply(this.error()));
        }
    }

    /**
     * Transforms the successful value of this result using the provided function 'f',
     * or leaves the result unchanged if it is a failure.
     * <p>
     * This method enables chaining of operations where each operation could return a Result.
     *
     * @param f the transformation function, should not be null.
     *          This function is applied if this result is a success.
     * @return a new Result instance transformed by function 'f' if this result is a success;
     * the same instance (without any transformation) if it is a failure.
     * @throws NullPointerException if 'f' is null.
     */
    <U> Result<U, E> flatMap(Function<R, Result<U, E>> f);

    /**
     * If this result is a success and the tested value matches the given predicate,
     * it returns the same result; otherwise it returns a failure with the provided error.
     * <p>
     * This method provides a way to enforce additional conditions on the successful result value.
     *
     * @param f     the Predicate instance that tests the successful value.
     *              This predicate is applied if this result is a success.
     * @param error the error instance to be used for creating a new failure Result, if the predicate test fails.
     *              It should not be null.
     * @return the same Result if it is either a failure or a success that matches the predicate;
     * a new failure Result with 'error' if it is a success that doesn't match the predicate.
     * @throws NullPointerException if 'f' or 'error' is null.
     */
    default Result<R, E> filter(Predicate<R> f, E error) {
        Objects.requireNonNull(f, "Predicate should not be null");
        return this.flatMap(x -> {
            if (f.test(x)) {
                return this;
            } else {
                return failure(error);
            }
        });
    }

    /**
     * Returns the error value when the result is a failure.
     *
     * @return the error instance associated with a failure result.
     * @throws IllegalStateException if the result is a success.
     */

    E error();

    /**
     * Checks whether this result is a success or failure.
     *
     * @return true if this result is a success; false otherwise.
     */
    boolean isSuccessful();

    /**
     * Returns true if the result is not successful, false otherwise.
     *
     * @return true if the result is not successful, false otherwise
     */
    default boolean isFailure() {
        return !isSuccessful();
    }

    /**
     * If the result is successful, it performs given action on the success value.
     *
     * @param f a Consumer instance that needs to be executed if the result is a success; it should not be null.
     */
    void ifSuccessful(Consumer<R> f);

    /**
     * If the result is successful, it performs given action on the success value; otherwise it executes the Runnable 'g'.
     *
     * @param f a Consumer instance that needs to be executed if the result is a success.
     * @param g a Runnable instance that needs to be executed if the result is a failure.
     *          Both `f` and `g` should not be null.
     */
    void ifSuccessfulOrElse(Consumer<R> f, Runnable g);

    /**
     * Transforms the successful values of this result and another given result using the provided function 'f' and
     * collects errors into a list if there are any in the individual results.
     *
     * @param o the other Result instance.
     * @param f a BiFunction that is applied to the successful values of the two results if both results are successful.
     *          It must not be null.
     * @return a success Result transformed by 'f' if both results are successful,
     * otherwise a failure Result with list of all error values from both results.
     * @throws NullPointerException if 'o' or 'f' is null.
     */
    default <V, G> Result<V, List<E>> map2(Result<G, E> o, BiFunction<R, G, V> f) {
        return this.mapError(Arrays::asList).map2(o.mapError(Arrays::asList), f, (x, y) -> Stream.concat(x.stream(), y.stream()).toList());
    }

    /**
     * Combines the values of two Results using a BiFunction, and maps the result to a new Result with a different type.
     *
     * @param <V>         the type of the resulting value
     * @param <G>         the type of the value in the other Result
     * @param o           the other Result to combine with
     * @param f           the BiFunction to apply to the values of the Results
     * @param errorMapper the BinaryOperator to map the error if both Results are failures
     * @return a new Result with the combined and mapped value, or the mapped error if both Results are failures
     * @throws NullPointerException if any of the parameters is null
     */
    default <V, G> Result<V, E> map2(Result<G, E> o, BiFunction<R, G, V> f, BinaryOperator<E> errorMapper) {
        Objects.requireNonNull(o, "Other should not be null");
        Objects.requireNonNull(f, "Mapper should not be null");
        Objects.requireNonNull(errorMapper, "error should not be null");

        if (!this.isSuccessful() && !o.isSuccessful()) {
            return failure(errorMapper.apply(this.error(), o.error()));
        }
        if (!this.isSuccessful()) {
            return failure(this.error());
        }
        if (!o.isSuccessful()) {
            return failure(o.error());
        }
        return success(f.apply(this.getOrThrow(), o.getOrThrow()));
    }

    /**
     * Recovers from an error by applying a recovery function.
     *
     * @param f the recovery function to apply
     * @return the new Result object with the error recovered, or the original Result if it is already successful
     * @throws NullPointerException if the recovery function is null
     */
    default Result<R, E> recover(Function<E, R> f) {
        Objects.requireNonNull(f, "Recovery function should not be null");
        if (this.isSuccessful()) {
            return this;
        } else {
            return success(f.apply(this.error()));
        }
    }

    /**
     * Tries to recover from the error in the current Result by applying the given recovery function.
     * If the current Result is successful, it is returned as-is.
     * If the current Result is a failure, the recovery function is applied to the error value and a new Result is returned.
     *
     * @param f the recovery function to be applied to the error value
     * @return a new Result obtained by applying the recovery function to the error value, or the current Result if it is successful
     * @throws NullPointerException if the recovery function is null
     */
    default Result<R, E> recoverWith(Function<E, Result<R, E>> f) {
        Objects.requireNonNull(f, "Recovery function should not be null");
        if (this.isSuccessful()) {
            return this;
        } else {
            return f.apply(this.error());
        }
    }

    class Failure<R, E> implements Result<R, E> {


        private final E error;

        private Failure(E message) {
            this.error = message;
        }

        @Override
        public String toString() {
            return String.format("Failure(%s)", error);
        }


        @Override
        public R getOrThrow(Supplier<RuntimeException> e) {
            throw e.get();
        }

        @Override
        public <U> Result<U, E> flatMap(Function<R, Result<U, E>> f) {
            return failure(error);
        }

        @Override
        public E error() {
            return error;
        }

        @Override
        public boolean isSuccessful() {
            return false;
        }

        @Override
        public void ifSuccessful(Consumer<R> f) {
            // Do nothing
        }

        @Override
        public void ifSuccessfulOrElse(Consumer<R> f, Runnable g) {
            g.run();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Failure<?, ?> failure = (Failure<?, ?>) o;
            return Objects.equals(error, failure.error);
        }

        @Override
        public int hashCode() {
            return Objects.hash(error);
        }

    }

    class Success<R, E> implements Result<R, E> {


        private final R value;

        private Success(R value) {
            super();
            this.value = value;
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }

        @Override
        public String toString() {
            return String.format("Success(%s)", value.toString());
        }

        @Override
        public R getOrThrow(Supplier<RuntimeException> e) {
            return value;
        }

        @Override
        public <U> Result<U, E> flatMap(Function<R, Result<U, E>> f) {
            return f.apply(value);
        }

        @Override
        public E error() {
            throw new IllegalStateException("No Error");
        }

        @Override
        public boolean isSuccessful() {
            return true;
        }

        @Override
        public void ifSuccessful(Consumer<R> f) {
            f.accept(value);
        }

        @Override
        public void ifSuccessfulOrElse(Consumer<R> f, Runnable g) {
            ifSuccessful(f);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Success<?, ?> success = (Success<?, ?>) o;
            return Objects.equals(value, success.value);
        }
    }
}