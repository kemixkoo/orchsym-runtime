package com.orchsym.web.listener;

import java.io.File;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.orchsym.util.BrandingProperties;

/**
 * @author GU Guoqiang
 *
 */
public class WebResourcesSyncContextListener implements ServletContextListener {
    private static final String IMG_FOLDER = "images";

    private final BrandingProperties brandingProp = new BrandingProperties();

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        final File webImagesFolder = getImageFolder(sce);

        brandingProp.syncWebImages(webImagesFolder);

    }

    private File getImageFolder(ServletContextEvent sce) {
        if (sce != null) {
            final String imagesPath = sce.getServletContext().getRealPath("/" + IMG_FOLDER);
            if (imagesPath != null) {
                File imagesFolder = new File(imagesPath);
                if (imagesFolder.exists()) {
                    return imagesFolder;
                }
            }
        }

        File classedRoot = new File(this.getClass().getClassLoader().getResource("/").getPath()); // .../WEB-INF/classes/
        if (classedRoot.exists()) {
            final File rootFolder = classedRoot.getParentFile().getParentFile();
            File imagesFolder = new File(rootFolder, IMG_FOLDER);
            if (imagesFolder.exists()) {
                return imagesFolder;
            }
        }
        return null;
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        //

    }

}
