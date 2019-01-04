pragma solidity >=0.4.22 <0.6.0;
contract EventStorage {
    event DataEvent (
        // unique identifier: hash(capability)
        uint indexed identifier,

        // encrypted data
        bytes payload
    );

    function newData(uint identifier, bytes memory payload) public {
        emit DataEvent(identifier, payload);
    }
}