package Bin.Networking.Protocol;

/**
 * Basic object pool
 * For increase performance only
 * And just wanted to try it
 *
 * Use only static methods
 * Firstly must be initialised
 */

public abstract class AbstractDataPackagePool {

    /**
     * Private constructor because all methods are static
     */

    private static AbstractDataPackagePool pool;

    /**
     * Must call it before start working with it
     * @param poolToSet instance which will define methods
     */

    public static void init(AbstractDataPackagePool poolToSet){
        if (pool != null){
            throw new IllegalStateException("Already initialised with " + pool);
        }
        pool = poolToSet;
    }

    /**
     * Use this method when you need a package
     * @return ready to use data package
     */

    public static AbstractDataPackage getPackage(){
        return pool.getPackageInst();
    }

    /**
     * Call when you need to return package to the home
     * @param dataPackage to be returned
     */

    public static void returnPackage(AbstractDataPackage dataPackage){
        pool.returnPackageInst(dataPackage);
    }

    /**
     * Call when you have many instance of package in storage
     */

    public static void clearStorage(){
        pool.clearInst();
    }

    /**
     * Ovveride it on your implementation
     * @return package ready to use
     */

    protected abstract AbstractDataPackage getPackageInst();

    /**
     * Ovveride it on your implementation
     * @param dataPackage to be returned
     */

    protected abstract void returnPackageInst(AbstractDataPackage dataPackage);

    /**
     * Ovveride it on your implementation
     * Clears an underlying storage with AbstractDataPackage
     */

    abstract void clearInst();
}
