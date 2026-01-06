package com.gym.app.service;

import java.io.File;

/**
 * Interface defining the contract for Face Recognition operations.
 * Allows switching between different providers (e.g., Luxand, Azure, AWS)
 * without changing app logic.
 */
public interface FaceRecognitionService {

    /**
     * Enrolls a face into the system.
     * 
     * @param name  The name of the person (or subject ID) to associate with the
     *              face.
     * @param photo The photo file containing the face image.
     * @return A unique face token or ID returned by the external API.
     * @throws Exception if enrollment fails (e.g., network error, no face
     *                   detected).
     */
    String enrollFace(String name, File photo) throws Exception;

    /**
     * Recognizes a face from a provided photo.
     * 
     * @param photo The photo file to analyze and compare against the database.
     * @return The name of the recognized person, or null if not
     *         recognized/uncertain.
     * @throws Exception if the recognition process encounters an error.
     */
    String recognizeFace(File photo) throws Exception;

    /**
     * Checks if the service is properly configured (e.g., has a valid API key).
     * 
     * @return true if API key is set and valid, false otherwise.
     */
    boolean isConfigured();
}
