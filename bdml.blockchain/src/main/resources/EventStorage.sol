pragma solidity >=0.4.22 <0.6.0;
contract EventStorage {
    event DataEvent (
        // 32 bytes identifier: hash(capability)
        uint indexed identifier,

        bytes frame
    );

    function newData(uint identifier, bytes memory frame) public {
        emit DataEvent(identifier, frame);
    }
}