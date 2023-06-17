package peeling.project.basic.config.jwt;

public interface JwtVO {
    public static final String SECRET = "d5U6B7vayxh6FlPGGdZEFEWLaPZj7TqbPrygmYF5XLQe9AcQu3BnVD5wjPD4WACm5uDJSKnETVnfn1hvi7MOVzsrAVJiaexPzEWItq4dXD9nozcP96rWHbqTHE8ynQsxTu8Y2IrQhNdbbCR7kpc2QPMvfF2tSNAR9CX54RNTh4B3QHoSC7JMCkGfS61NAgTzaU6LIdF80sGNGwqInHsIJD9V86AOmloXvcOHXFUiWzt6tH4SPdEQ27tJR69SPyIL";
//    public static final int EXPIRATION_TIME = 1000 * 60 * 60 * 24 * 7; //1주일
    public static final int EXPIRATION_TIME = 30000; //1주일
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String HEADER = "Authorization";
}
