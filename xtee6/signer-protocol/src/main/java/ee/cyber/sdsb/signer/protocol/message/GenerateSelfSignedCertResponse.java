package ee.cyber.sdsb.signer.protocol.message;

import java.io.Serializable;

import lombok.ToString;
import lombok.Value;

/**
 * Signer API message.
 */
@Value
@ToString(exclude = "certificateBytes")
public class GenerateSelfSignedCertResponse implements Serializable {

    private final byte[] certificateBytes;

}