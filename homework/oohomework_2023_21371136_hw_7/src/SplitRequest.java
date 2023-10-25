public class SplitRequest {
    private Integer personId;
    private Integer fromIndex;
    private Integer toIndex;
    private Integer elevatorIndex;

    public SplitRequest(Integer personId, Integer fromIndex,
                        Integer toIndex, Integer elevatorIndex) {
        this.personId = personId;
        this.fromIndex = fromIndex;
        this.toIndex = toIndex;
        this.elevatorIndex = elevatorIndex;
    }

    public Integer getPersonId() {
        return personId;
    }

    public Integer getFromIndex() {
        return fromIndex;
    }

    public Integer getToIndex() {
        return toIndex;
    }

    public Integer getElevatorIndex() {
        return elevatorIndex;
    }

    @Override
    public String toString() {
        return (elevatorIndex + 1) + ": " + (fromIndex + 1) + "->" + (toIndex + 1);
    }
}
