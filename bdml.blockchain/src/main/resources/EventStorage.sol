pragma solidity >=0.4.22 <0.6.0;
contract EventStorage {
    event SecretDataEvent (
        // 32 bytes identifier: hash(capability)
        uint indexed identifier,
        bytes document
    );

    event PublicDataEvent (
        // 32 bytes identifier: hash(capability)
        uint indexed identifier,
        bytes document
    );

    event AccessEvent (
        uint160 indexed identifier,
        bytes token
    );

    event AmendEvent (
        uint160 indexed identifier,
        bytes token
    );


    //Todo: BlobEvent -- combining N AccessTokens + M Documents

    function newSecretData(uint identifier, bytes memory document) public {
        emit SecretDataEvent(identifier, document);
    }

    function newPublicData(uint identifier, bytes memory document) public {
        emit PublicDataEvent(identifier, document);
    }

    function newAccess(uint160 identifier, bytes memory token) public {
        emit AccessEvent(identifier, token);
    }

    function newAmend(uint160 identifier, bytes memory token) public {
        emit AmendEvent(identifier, token);
    }

}