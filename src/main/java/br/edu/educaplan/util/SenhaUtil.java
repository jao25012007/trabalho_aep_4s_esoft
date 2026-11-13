package br.edu.educaplan.util;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/** PBKDF2 com sal individual; o banco nunca recebe a senha original. */
public final class SenhaUtil {
    private static final int ITERACOES = 600_000;
    private SenhaUtil() {}
    public static String gerarHash(char[] senha) {
        Validacao.senha(senha);
        byte[] sal = new byte[16];
        new SecureRandom().nextBytes(sal);
        return "pbkdf2_sha256$" + ITERACOES + "$" + Base64.getEncoder().encodeToString(sal)
            + "$" + Base64.getEncoder().encodeToString(derivar(senha, sal, ITERACOES));
    }
    public static boolean verificar(char[] senha, String hash) {
        if (senha == null || hash == null) return false;
        try {
            String[] partes = hash.split("\\$");
            if (partes.length != 4 || !partes[0].equals("pbkdf2_sha256")) return false;
            int iteracoes = Integer.parseInt(partes[1]);
            if (iteracoes < 100_000 || iteracoes > 2_000_000) return false;
            byte[] esperado = Base64.getDecoder().decode(partes[3]);
            byte[] obtido = derivar(senha, Base64.getDecoder().decode(partes[2]), iteracoes);
            try { return MessageDigest.isEqual(esperado, obtido); }
            finally { Arrays.fill(obtido, (byte) 0); }
        } catch (IllegalArgumentException e) { return false; }
    }
    private static byte[] derivar(char[] senha, byte[] sal, int iteracoes) {
        PBEKeySpec spec = new PBEKeySpec(senha, sal, iteracoes, 256);
        try { return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded(); }
        catch (java.security.GeneralSecurityException e) { throw new IllegalStateException("Falha ao proteger a senha.", e); }
        finally { spec.clearPassword(); }
    }
}
