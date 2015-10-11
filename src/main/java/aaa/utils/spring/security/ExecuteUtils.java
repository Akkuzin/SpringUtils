package aaa.utils.spring.security;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.concurrent.Callable;
import java.util.function.Supplier;

@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ExecuteUtils {

	public interface CustomCallable<V, E extends Exception> extends Callable<V> {
		@Override
		V call() throws E;
	}

	public static <V> CustomCallable<V, RuntimeException> fromLambda(Supplier<V> lambda) {
		return new CustomCallable<V, RuntimeException>() {
			@Override
			public V call() {
				return lambda.get();
			}
		};
	}

	public interface CustomRunnable<E extends Exception> {
		void run() throws E;
	}

	Authentication authentication;

	public static ExecuteUtils withAuth(Authentication providedAuthentication) {
		return new ExecuteUtils(providedAuthentication);
	}

	public <V, E extends Exception> V doCall(CustomCallable<V, E> callable) throws E {
		return execWithAuth(authentication, callable);
	}

	public <V, E extends Exception> void doRun(CustomCallable<Void, E> callable) throws E {
		execWithAuth(authentication, callable);
	}

	public <E extends Exception> void doRun(final CustomRunnable<E> runnable) throws E {
		execWithAuth(authentication, new CustomCallable<Void, E>() {
			@Override
			public Void call() throws E {
				runnable.run();
				return null;
			}
		});
	}

	public static <V, E extends Exception> V execWithAuth(	Authentication providedAuthentication,
															CustomCallable<V, E> callable) throws E {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		try {
			SecurityContextHolder.getContext().setAuthentication(providedAuthentication);
			return callable.call();
		} finally {
			SecurityContextHolder.getContext().setAuthentication(authentication);
		}
	}

}
