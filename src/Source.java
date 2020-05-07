public class Source {

    private String name;

    public void setName(String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }

    private boolean add;

    public void setAdd(boolean add) {
        this.add = add;
    }
    public boolean isAdd() {
        return add;
    }

    private double monthlyRate;

    public void setMonthlyRate(double monthlyRate) {
        this.monthlyRate = monthlyRate;
    }
    public double getMonthlyRate() {
        return monthlyRate;
    }

    private double yearlyRate;

    public void setYearlyRate(double yearlyRate) {
        this.yearlyRate = yearlyRate;
    }
    public double getYearlyRate() {
        return yearlyRate;
    }

    //How much of the person's income total this source either takes or adds
    private double percentOfTotalYear;

    public void setPercentOfTotalYear(double percentOfTotalYear) {
        this.percentOfTotalYear = percentOfTotalYear;
    }
    public double getPercentOfTotalYear() {
        return percentOfTotalYear;
    }

    /**
     *
     * @param title - Title of the person's income source
     * @param input - Whether the source is going to add to the person's income total or subtract from it
     * @param rate - Monthly rate of which the source takes/adds at
     */
    public Source(String title, boolean input, double rate){
        name = title;
        add = input;
        monthlyRate = rate;
    }

    public String toString(){
        return name + " " + add + " " + monthlyRate;
    }
}
