package data;

/**
 * Created by 3yanlis1bos on 3/15/2016.
 */
public class Plate {

    private int id;
    private String plate;
    private String record;

    public Plate(){}

    public Plate(String plate, String record) {
        super();
        this.plate = plate;
        this.record = record;
    }

    public int getId() {
        return id;
    }

    public String getPlate() {
        return plate;
    }

    public String getRecord() {
        return record;
    }
    //getters & setters

    public void setId(int id) {
        this.id = id;
    }

    public void setPlate(String plate) {
        this.plate = plate;
    }

    public void setRecord(String record) {
        this.record = record;
    }

    @Override
    public String toString() {
        return "Plate [id=" + id + ", plate=" + this.getPlate() + ", record=" + this.getRecord()
                + "]";
    }
}