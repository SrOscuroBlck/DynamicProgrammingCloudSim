package org.example;

import org.cloudsimplus.cloudlets.Cloudlet;
import org.cloudsimplus.cloudlets.CloudletSimple;
import org.cloudsimplus.utilizationmodels.UtilizationModelFull;

import java.util.ArrayList;
import java.util.List;

public class ComplexTaskGenerator {
    public static List<Cloudlet> createComplexCloudlets(int numberOfCloudlets) {
        List<Cloudlet> cloudletList = new ArrayList<>();
        for (int i = 0; i < numberOfCloudlets; i++) {
            long length = 10000 + (i * 1000);
            int pesNumber = 1 + (i % 4);
            long fileSize = 300 + (i * 100);
            long outputSize = 300 + (i * 100);

            Cloudlet cloudlet = new CloudletSimple(length, pesNumber)
                    .setFileSize(fileSize)
                    .setOutputSize(outputSize)
                    .setUtilizationModelCpu(new UtilizationModelFull())
                    .setUtilizationModelRam(new UtilizationModelFull())
                    .setUtilizationModelBw(new UtilizationModelFull());

            cloudletList.add(cloudlet);
        }
        return cloudletList;
    }
}
