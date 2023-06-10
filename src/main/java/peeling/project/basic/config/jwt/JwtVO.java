package peeling.project.basic.config.jwt;

public interface JwtVO {
    public static final String SECRET = "peeling.project.secret_key";
    public static final int EXPIRATION_TIME = 1000 * 60 * 60 * 24 * 7; //1주일
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String HEADER = "Authorization";
}
