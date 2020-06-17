package tech.pegasys.teku.phase1.integration.bls;

import static com.google.common.base.Preconditions.checkArgument;

import org.apache.milagro.amcl.BLS381.BIG;
import org.apache.milagro.amcl.BLS381.ECP2;
import org.apache.milagro.amcl.BLS381.FP2;
import org.apache.milagro.amcl.BLS381.ROM;
import org.apache.tuweni.bytes.Bytes;
import tech.pegasys.teku.bls.hashToG2.HashToCurve;

/*
 * Copied from the ConsenSys/mikuli (Apache 2 License) implementation:
 * https://github.com/ConsenSys/mikuli/blob/master/src/main/java/net/consensys/mikuli/crypto/*.java
 */
public interface G2Points {

  BIG P = new BIG(ROM.Modulus);
  int fpPointSize = BIG.MODBYTES;

  /**
   * Deserialize the point from compressed form.
   *
   * <p>The standard follows the ZCash format for serialization documented here:
   * https://github.com/zkcrypto/pairing/blob/master/src/bls12_381/README.md#serialization
   *
   * @param bytes the compressed serialized form of the point
   * @return the point
   */
  static ECP2 fromBytesCompressed(Bytes bytes) {
    checkArgument(
        bytes.size() == 2 * fpPointSize,
        "Expected %s bytes but received %s",
        2 * fpPointSize,
        bytes.size());
    byte[] xImBytes = bytes.slice(0, fpPointSize).toArray();
    byte[] xReBytes = bytes.slice(fpPointSize, fpPointSize).toArray();

    boolean aIn = (xImBytes[0] & (byte) (1 << 5)) != 0;
    boolean bIn = (xImBytes[0] & (byte) (1 << 6)) != 0;
    boolean cIn = (xImBytes[0] & (byte) (1 << 7)) != 0;
    xImBytes[0] &= (byte) 31;

    if ((xReBytes[0] & (byte) 224) != 0) {
      throw new IllegalArgumentException("The input has non-zero a2, b2 or c2 flag on xRe");
    }

    if (!cIn) {
      throw new IllegalArgumentException("The serialized input does not have the C flag set.");
    }

    if (bIn) {
      if (!aIn && Bytes.wrap(xImBytes).isZero() && Bytes.wrap(xReBytes).isZero()) {
        // This is a correctly formed serialization of infinity
        return new ECP2();
      } else {
        // The input is malformed
        throw new IllegalArgumentException(
            "The serialized input has B flag set, but A flag is set, or X is non-zero.");
      }
    }

    // We must check that x < q (the curve modulus) for this serialization to be valid
    // We raise an exception (that should be caught) if this check fails: somebody might feed us
    // faulty input.
    BIG xImBig = BIG.fromBytes(xImBytes);
    BIG xReBig = BIG.fromBytes(xReBytes);
    BIG modulus = new BIG(ROM.Modulus);
    if (BIG.comp(modulus, xReBig) <= 0 || BIG.comp(modulus, xImBig) <= 0) {
      throw new IllegalArgumentException(
          "The deserialized X real or imaginary coordinate is too large.");
    }

    ECP2 point = new ECP2(new FP2(xReBig, xImBig));

    if (point.is_infinity()) {
      throw new IllegalArgumentException("X coordinate is not on the curve.");
    }

    if (!isInGroup(point)) {
      throw new IllegalArgumentException("The deserialized point is not in the G2 subgroup.");
    }

    // Did we get the right branch of the sqrt?
    if (aIn != calculateYFlag(point.getY().getB())) {
      // We didn't: so choose the other branch of the sqrt.
      FP2 x = point.getX();
      FP2 yneg = point.getY();
      yneg.neg();
      point = new ECP2(x, yneg);
    }

    return point;
  }

  /**
   * Verify that the given point is in the correct subgroup for G2.
   *
   * @param point The elliptic curve point
   * @return True if the point is in G2; false otherwise
   */
  static boolean isInGroup(ECP2 point) {
    return HashToCurve.isInGroupG2(point);
  }

  /**
   * Calculate (y_im * 2) // q (which corresponds to the a1 flag in the Eth2 BLS spec)
   *
   * <p>This is used to disambiguate Y, given X, as per the spec. P is the curve modulus.
   *
   * @param yIm the imaginary part of the Y coordinate of the point
   * @return true if the a1 flag and yIm correspond
   */
  static boolean calculateYFlag(BIG yIm) {
    BIG tmp = new BIG(yIm);
    tmp.add(yIm);
    tmp.div(P);
    return tmp.isunity();
  }
}
